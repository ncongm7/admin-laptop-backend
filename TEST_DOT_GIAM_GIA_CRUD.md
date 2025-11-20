# TEST CRUD ĐỢT GIẢM GIÁ

## Các API Endpoints

### 1. GET - Lấy danh sách
```
GET http://localhost:8080/api/dot-giam-gia-quan-ly/danh-sach
```

### 2. GET - Lấy chi tiết
```
GET http://localhost:8080/api/dot-giam-gia-quan-ly/detail/{id}
```

### 3. POST - Thêm mới (Loại %)
```json
POST http://localhost:8080/api/dot-giam-gia-quan-ly/add
Content-Type: application/json

{
  "tenKm": "Test Giảm giá 10%",
  "loaiDotGiamGia": 1,
  "giaTri": 10.00,
  "soTienGiamToiDa": 2000000.00,
  "moTa": "Test giảm giá 10% tối đa 2 triệu",
  "ngayBatDau": "2025-01-20T00:00:00Z",
  "ngayKetThuc": "2025-12-31T23:59:59Z",
  "trangThai": 1
}
```

### 4. POST - Thêm mới (Loại VND)
```json
POST http://localhost:8080/api/dot-giam-gia-quan-ly/add
Content-Type: application/json

{
  "tenKm": "Test Giảm giá 500k",
  "loaiDotGiamGia": 2,
  "giaTri": 500000.00,
  "soTienGiamToiDa": 500000.00,
  "moTa": "Test giảm giá cố định 500k",
  "ngayBatDau": "2025-01-20T00:00:00Z",
  "ngayKetThuc": "2025-12-31T23:59:59Z",
  "trangThai": 1
}
```

### 5. PUT - Cập nhật
```json
PUT http://localhost:8080/api/dot-giam-gia-quan-ly/update/{id}
Content-Type: application/json

{
  "tenKm": "Test Giảm giá 15% (Updated)",
  "loaiDotGiamGia": 1,
  "giaTri": 15.00,
  "soTienGiamToiDa": 3000000.00,
  "moTa": "Test giảm giá 15% tối đa 3 triệu (Updated)",
  "ngayBatDau": "2025-01-20T00:00:00Z",
  "ngayKetThuc": "2025-12-31T23:59:59Z",
  "trangThai": 1
}
```

### 6. DELETE - Xóa
```
DELETE http://localhost:8080/api/dot-giam-gia-quan-ly/delete/{id}
```

## Test Cases

### Test 1: Thêm mới - Loại % (1)
- ✅ Loại = 1 (%)
- ✅ Giá trị = 10.00 (%)
- ✅ Số tiền giảm tối đa = 2,000,000 VND
- ✅ Validation: giá trị 0-100

### Test 2: Thêm mới - Loại VND (2)
- ✅ Loại = 2 (VND)
- ✅ Giá trị = 500,000 VND
- ✅ Số tiền giảm tối đa tự động = giá trị (500,000)

### Test 3: Cập nhật - Đổi từ VND sang %
- ✅ Đổi loại từ 2 (VND) sang 1 (%)
- ✅ Cập nhật giá trị và số tiền giảm tối đa

### Test 4: Validation
- ❌ Giá trị % > 100 → Lỗi
- ❌ Giá trị % < 0 → Lỗi
- ❌ Giá trị VND < 0 → Lỗi
- ❌ Thiếu loại giảm giá → Lỗi

### Test 5: Xóa
- ✅ Xóa đợt giảm giá thành công

