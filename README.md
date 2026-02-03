# ClaimLand

Enterprise-grade land claim plugin for Paper/Folia + proxy environments.

## Features ✅

- Chunk-based land claims via golden shovel
- SQLite default storage (optional MySQL)
- Vault economy tax system (weekly taxes, auto-unclaim)
- Action bar land info on chunk enters
- Inventory GUI (land info, members, permissions, tax)
- PlaceholderAPI expansion
- Folia-safe scheduling and async DB access
- Permissions and ranks via `ranks.yml`

---

## Permissions (LuckPerms recommended) ⚙️

- `claimland.use` - basic plugin usage
- `claimland.land.claim` - claim a chunk
- `claimland.land.unclaim` - unclaim current chunk
- `claimland.land.gui` - open land GUI
- `claimland.admin` - admin access
- `claimland.bypass` - bypass protections

Use LuckPerms to assign permissions and create rank-based limits (configure `ranks.yml`).

---

## Commands

Player:
- `/land claim [name]`
- `/land unclaim`
- `/land info`
- `/land gui`
- `/land addmember <player>`
- `/land delmember <player>`

Admin:
- `/landadmin list <player>`
- `/landadmin info <land-id>`
- `/landadmin delland <land-id>`
- `/landadmin transfer <land-id> <player>`
- `/landadmin settax <land-id> <amount>`
- `/landadmin forceclaim`
- `/landadmin bypass`
- `/landadmin audit`
- `/landadmin reload`

---

## Installation

1. Build the plugin: `./gradlew shadowJar` (produces `build/libs/claimland-0.1.0.jar`) or `./gradlew jar` for a fallback JAR.
2. Upload the `claimland-0.1.0.jar` (or `claimland.jar`) to your server `plugins/` folder or attach it to a GitHub Release.
3. Install dependencies: `Vault` (for economy), `LuckPerms` (permissions/ranks), `PlaceholderAPI` (optional).
4. Start server, then edit `config.yml`, `ranks.yml`, and `permissions.yml`.

### LuckPerms examples

- Give a player basic permission:
  - `/lp user <player> permission set claimland.use true`
- Create a rank `vip` with permission:
  - `/lp creategroup vip`
  - `/lp group vip permission set claimland.land.claim true`
  - `/lp group vip meta set max-lands 10`

---

---

## Building & Releasing

- Build locally with Gradle wrapper: `./gradlew build` or `./gradlew shadowJar`.
- To publish on GitHub: create a release and attach the generated JAR file.
- Typical git workflow:
  - `git add .` `git commit -m "Add claimland"` `git push` then create release on GitHub and upload JAR.

---

## Development Notes

- Default storage: SQLite. To enable MySQL, update `config.yml`.
- PlaceholderAPI supported via `claimland` expansion (use `%claimland_land_count%`).
- If you want to avoid AI, don't look at the AI-generated notes: "DON'T LOOK AI!" (user note)

---

## License

MIT License

