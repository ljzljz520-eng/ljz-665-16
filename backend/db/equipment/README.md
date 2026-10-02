# 设备档案模块 — 数据库脚本

| 文件 | 用途 | 适用数据库 |
| --- | --- | --- |
| `equipment_init_append.sql` | 全新初始化脚本，已接入 `db/sqlserver/entrypoint.sh`，随 `docker compose up` 自动执行 | MySQL / SQL Server（经仓库转换器转换） |
| `equipment_archive_mysql.sql` | 仅建表（幂等），已有环境手动执行 | MySQL |
| `equipment_archive_sqlserver.sql` | 仅建表（幂等），已有环境手动执行 | SQL Server |
| `equipment_menu_mysql.sql` | 菜单 + 管理员授权（幂等），已有环境手动执行 | MySQL |
| `equipment_menu_sqlserver.sql` | 菜单 + 管理员授权（幂等），已有环境手动执行 | SQL Server |

## 已有环境升级步骤

1. 执行对应的建表脚本（MySQL 用 `equipment_archive_mysql.sql`，SQL Server 用 `equipment_archive_sqlserver.sql`）。
2. 执行对应的菜单脚本（MySQL 用 `equipment_menu_mysql.sql`，SQL Server 用 `equipment_menu_sqlserver.sql`）。
3. 重新部署后端、前端；以 admin 登录后即可看到"设备管理 → 设备档案"菜单。
