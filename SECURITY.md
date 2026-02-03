# Security & Exploit Prevention

This plugin implements multiple layers of protection to reduce grief and exploits:

- Event validation for block placement/break, piston movements, hopper transfers, explosions, fire spread
- Audit logging of denied actions stored in `audit` table (time, land_id, player, action, details)
- Folia-safe scheduler abstraction to avoid async violations on region-threaded servers
- Async-safe DB I/O via HikariCP and CompletableFuture
- Configuration validation at startup and safe defaults
- Permission-based bypass for admins

If you discover a security issue, please open an issue or submit a PR. Follow responsible disclosure.
