# NgaoMaternal Care — Android App

A native Android (Kotlin + Jetpack Compose) companion app for the **NgaoMaternal Care** web
project (https://github.com/cjdreamy/NgaoMaternal-Care). It talks to the **same Supabase
backend** as the web app, so mothers and healthcare providers can use whichever client they
have access to.

Features implemented, mirroring the web app:

- **Auth**: Sign up (choose "Expectant Mother" or "Healthcare Provider") and log in, backed by
  Supabase Auth.
- **Mother dashboard**: Pregnancy week, recent check-ins (last 7 days), next visit, active
  alerts, a Daily Health Check-in flow, and the **Emergency Panic Button** (grabs GPS location
  and files an alert).
- **Daily health check-in**: BP, heart rate, fetal movement, a warning-signs checklist
  (vaginal bleeding, severe headache, reduced fetal movement, etc.). Anything matching a
  danger sign is auto-flagged/critical and raises an alert for providers, same as the web app.
- **Provider dashboard**: Active alerts / flagged check-ins / critical cases / total patients
  stat cards, with tabs for Flagged Check-ins (Mark as Reviewed), Emergency Alerts (Mark
  Resolved), Patients, and Recent Activity.
- **Educational Resources**: Category-filterable prenatal education content (General, Safety,
  Nutrition, Health, Monitoring, Preparation).

---

## 1. Point it at your Supabase project

Open `app/build.gradle.kts` and fill in your project's values (Supabase dashboard → **Project
Settings → API**):

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://YOUR-PROJECT-REF.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"YOUR-SUPABASE-ANON-KEY\"")
```

Use the same project your web app (`ngao-maternal-care.vercel.app`) already points to, so
both clients share the same users and data.

## 2. Database schema

The app expects these tables. If your existing Supabase project already has equivalent
tables from the web app, adjust the Kotlin models/field names in
`data/model/Models.kt` and `data/remote/SupabaseRestApi.kt` to match instead of
recreating tables. Otherwise, run this in the Supabase SQL editor:

```sql
-- Profiles: one row per authenticated user, links to auth.users
create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null,
  role text not null check (role in ('mother', 'provider')),
  phone text,
  created_at timestamptz default now()
);

-- Pregnancies: one active pregnancy record per mother
create table public.pregnancies (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  start_date date,
  due_date date,
  next_visit text,
  created_at timestamptz default now()
);

-- Daily health check-ins
create table public.checkins (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  user_name text,
  bp_systolic int,
  bp_diastolic int,
  heart_rate int,
  fetal_movement int,
  symptoms text[] default '{}',
  notes text,
  risk_level text not null default 'normal' check (risk_level in ('normal', 'flagged', 'critical')),
  reviewed boolean not null default false,
  created_at timestamptz default now()
);

-- Emergency alerts (panic button + auto-raised from critical check-ins)
create table public.alerts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  user_name text,
  type text not null default 'panic' check (type in ('panic', 'critical_checkin')),
  latitude double precision,
  longitude double precision,
  status text not null default 'active' check (status in ('active', 'resolved')),
  created_at timestamptz default now()
);

-- Prenatal education content, organized by week + category
create table public.education_content (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  category text not null,
  week int not null,
  body text not null
);
```

### Row Level Security (recommended)

```sql
alter table public.profiles enable row level security;
alter table public.pregnancies enable row level security;
alter table public.checkins enable row level security;
alter table public.alerts enable row level security;
alter table public.education_content enable row level security;

-- Everyone can read their own profile; providers can read all profiles
create policy "read own profile" on public.profiles
  for select using (auth.uid() = id);
create policy "providers read all profiles" on public.profiles
  for select using (exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'provider'));
create policy "insert own profile" on public.profiles
  for insert with check (auth.uid() = id);

-- Pregnancies: owner + providers
create policy "mother manages own pregnancy" on public.pregnancies
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "providers read pregnancies" on public.pregnancies
  for select using (exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'provider'));

-- Check-ins: owner can insert/read own; providers can read + update (mark reviewed) all
create policy "mother manages own checkins" on public.checkins
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "providers read all checkins" on public.checkins
  for select using (exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'provider'));
create policy "providers update checkins" on public.checkins
  for update using (exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'provider'));

-- Alerts: same pattern
create policy "mother manages own alerts" on public.alerts
  for all using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "providers read all alerts" on public.alerts
  for select using (exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'provider'));
create policy "providers update alerts" on public.alerts
  for update using (exists (select 1 from public.profiles p where p.id = auth.uid() and p.role = 'provider'));

-- Education content: readable by any authenticated user
create policy "anyone authenticated reads education" on public.education_content
  for select using (auth.role() = 'authenticated');
```

Seed a bit of education content (optional — the app also has built-in fallback content if this
table is empty):

```sql
insert into public.education_content (title, category, week, body) values
('Welcome to Your Pregnancy Journey', 'General', 1, 'Congratulations on your pregnancy! Regular check-ins and monitoring are essential for you and your baby''s health.'),
('Understanding Warning Signs', 'Safety', 4, 'Watch for: severe headaches, blurred vision, severe abdominal pain, reduced fetal movement, vaginal bleeding, or sudden swelling.'),
('Nutrition During Pregnancy', 'Nutrition', 8, 'Focus on iron-rich foods, calcium, protein, fruits and vegetables. Drink at least 8 glasses of water daily.'),
('Blood Pressure Awareness', 'Health', 20, 'High blood pressure during pregnancy can be dangerous. Monitor regularly and report concerns.'),
('Fetal Movement Monitoring', 'Monitoring', 28, 'From week 28, you should feel at least 10 movements in 2 hours.'),
('Preparing for Labor', 'Preparation', 36, 'Prepare your hospital bag, know the route to your clinic, and keep emergency contacts updated.');
```

## 3. Open in Android Studio

1. Download/unzip this project.
2. Android Studio → **Open** → select the `NgaoMaternalCare` folder.
3. Let Gradle sync (Studio will fetch the Gradle distribution defined in
   `gradle/wrapper/gradle-wrapper.properties` — Gradle 8.7 — and, if the wrapper jar itself is
   missing, Studio will offer to regenerate it; accept that prompt, or run `gradle wrapper`
   once from a terminal that has Gradle installed).
4. Fill in `SUPABASE_URL` / `SUPABASE_ANON_KEY` as described above.
5. Run on an emulator or device (▶ Run 'app'). Minimum SDK is Android 8.0 (API 26).

## 4. Project layout

```
app/src/main/java/com/ngao/maternalcare/
├── NgaoApp.kt                     # App-level singletons (session, repository)
├── MainActivity.kt
├── data/
│   ├── model/Models.kt            # Auth + domain models (profiles, checkins, alerts, ...)
│   ├── remote/                    # Retrofit clients for Supabase Auth + REST, session storage
│   └── repository/NgaoRepository.kt
├── ui/
│   ├── theme/                     # Compose theme matching the web app's dark navy palette
│   ├── nav/NgaoNavGraph.kt         # Navigation graph
│   └── screens/
│       ├── auth/                  # Splash, Login, Sign Up
│       ├── mother/                # Dashboard, Check-in
│       ├── provider/              # Provider dashboard + tabs
│       └── education/             # Educational resources
└── util/                          # Small shared helpers (Result wrapper, ViewModel factory)
```

## Notes / things you may want to extend

- **Panic button**: currently just files an `alerts` row with GPS coordinates. Wire up
  push notifications (e.g. Supabase Edge Function + FCM) to actually notify clinic staff in
  real time.
- **"View Patient"** buttons in the provider dashboard are stubbed — wire them to a patient
  detail screen once you decide what history/chart you want providers to see.
- The app uses direct REST calls (Retrofit) against Supabase's auto-generated API rather than
  the official `supabase-kt` SDK, to keep the dependency footprint small and avoid version
  churn — swap it out for `supabase-kt` if you'd prefer realtime subscriptions (e.g. for
  alerts pushing to providers instantly) instead of pull-to-refresh.
