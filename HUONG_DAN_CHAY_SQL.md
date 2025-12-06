# Hướng Dẫn Chạy File SQL

## Cách 1: Dùng SQL Server Management Studio (SSMS)

1. Mở SQL Server Management Studio
2. Kết nối đến SQL Server của bạn
3. Mở file `data-real.sql`
4. Chọn database `QuanLyBanHangLaptop_TheoERD1_New` (hoặc tạo mới nếu chưa có)
5. Nhấn F5 hoặc Execute để chạy

## Cách 2: Dùng sqlcmd (Command Line)

```powershell
sqlcmd -S localhost -d QuanLyBanHangLaptop_TheoERD1_New -i data-real.sql -E
```

Hoặc với username/password:
```powershell
sqlcmd -S localhost -U sa -P yourpassword -d QuanLyBanHangLaptop_TheoERD1_New -i data-real.sql
```

## Cách 3: Dùng PowerShell Script

```powershell
.\run_sql.ps1
```

**Lưu ý**: Sửa `$serverName` và `$databaseName` trong file `run_sql.ps1` nếu cần.

## Kiểm tra kết quả

Sau khi chạy xong, kiểm tra:
```sql
SELECT COUNT(*) FROM san_pham;  -- Phải có 20 sản phẩm
SELECT COUNT(*) FROM chi_tiet_san_pham;  -- Phải có 20 chi tiết sản phẩm
SELECT COUNT(*) FROM cpu;  -- Phải có các CPU từ JSON
```

