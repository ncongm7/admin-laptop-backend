# API Documentation - Quản lý Sản phẩm và Chi tiết Sản phẩm

## Tổng quan
Hệ thống API quản lý sản phẩm và chi tiết sản phẩm với tính năng tự động tạo biến thể dựa trên các thuộc tính đặc trưng.

## Các API Endpoints

### 1. Sản phẩm (SanPham)

#### Tạo sản phẩm mới
```
POST /api/san-pham
Content-Type: application/json

{
    "maSanPham": "SP001",
    "tenSanPham": "Laptop Gaming ASUS",
    "moTa": "Laptop gaming cao cấp",
    "trangThai": 1,
    "nguoiTao": "admin",
    "nguoiSua": "admin"
}
```

#### Lấy danh sách sản phẩm
```
GET /api/san-pham
GET /api/san-pham/page?page=0&size=10&sort=tenSanPham,asc
```

#### Tìm kiếm sản phẩm
```
GET /api/san-pham/search?tenSanPham=ASUS
GET /api/san-pham/trang-thai/1
GET /api/san-pham/search/advanced?trangThai=1&tenSanPham=Gaming
```

#### Cập nhật sản phẩm
```
PUT /api/san-pham/{id}
Content-Type: application/json

{
    "maSanPham": "SP001",
    "tenSanPham": "Laptop Gaming ASUS Updated",
    "moTa": "Laptop gaming cao cấp - Updated",
    "trangThai": 1,
    "nguoiSua": "admin"
}
```

#### Xóa sản phẩm
```
DELETE /api/san-pham/{id}
```

### 2. Chi tiết Sản phẩm (ChiTietSanPham)

#### Tạo chi tiết sản phẩm đơn lẻ
```
POST /api/chi-tiet-san-pham
Content-Type: application/json

{
    "idSanPham": "uuid-san-pham",
    "maCtsp": "CTSP001",
    "giaBan": 25000000,
    "ghiChu": "Sản phẩm mới",
    "soLuongTon": 100,
    "soLuongTamGiu": 0,
    "trangThai": 1,
    "idCpu": "uuid-cpu",
    "idGpu": "uuid-gpu",
    "idRam": "uuid-ram",
    "idOCung": "uuid-o-cung",
    "idMauSac": "uuid-mau-sac",
    "idLoaiManHinh": "uuid-loai-man-hinh",
    "idPin": "uuid-pin"
}
```

#### Tạo biến thể sản phẩm tự động
```
POST /api/chi-tiet-san-pham/tao-bien-the
Content-Type: application/json

{
    "idSanPham": "uuid-san-pham",
    "giaBan": 25000000,
    "ghiChu": "Tạo biến thể tự động",
    "soLuongTon": 50,
    "soLuongTamGiu": 0,
    "trangThai": 1,
    "selectedCpuIds": ["uuid-cpu-1", "uuid-cpu-2"],
    "selectedGpuIds": ["uuid-gpu-1", "uuid-gpu-2"],
    "selectedRamIds": ["uuid-ram-1"],
    "selectedOCungIds": ["uuid-o-cung-1", "uuid-o-cung-2"],
    "selectedMauSacIds": ["uuid-mau-1", "uuid-mau-2"],
    "selectedLoaiManHinhIds": ["uuid-man-hinh-1"],
    "selectedPinIds": ["uuid-pin-1"]
}
```

#### Lấy chi tiết sản phẩm theo sản phẩm
```
GET /api/chi-tiet-san-pham/san-pham/{sanPhamId}
```

#### Lấy danh sách chi tiết sản phẩm
```
GET /api/chi-tiet-san-pham
GET /api/chi-tiet-san-pham/page?page=0&size=10
```

### 3. Quản lý Thuộc tính Đặc trưng

#### CPU
```
GET /api/cpu
POST /api/cpu
PUT /api/cpu/{id}
DELETE /api/cpu/{id}
GET /api/cpu/trang-thai/{trangThai}
```

#### GPU
```
GET /api/gpu
POST /api/gpu
PUT /api/gpu/{id}
DELETE /api/gpu/{id}
GET /api/gpu/trang-thai/{trangThai}
```

#### RAM
```
GET /api/ram
POST /api/ram
PUT /api/ram/{id}
DELETE /api/ram/{id}
GET /api/ram/trang-thai/{trangThai}
```

#### Ổ cứng
```
GET /api/o-cung
POST /api/o-cung
PUT /api/o-cung/{id}
DELETE /api/o-cung/{id}
GET /api/o-cung/trang-thai/{trangThai}
```

#### Màu sắc
```
GET /api/mau-sac
POST /api/mau-sac
PUT /api/mau-sac/{id}
DELETE /api/mau-sac/{id}
GET /api/mau-sac/trang-thai/{trangThai}
```

#### Loại màn hình
```
GET /api/loai-man-hinh
POST /api/loai-man-hinh
PUT /api/loai-man-hinh/{id}
DELETE /api/loai-man-hinh/{id}
GET /api/loai-man-hinh/trang-thai/{trangThai}
```

#### Pin
```
GET /api/pin
POST /api/pin
PUT /api/pin/{id}
DELETE /api/pin/{id}
GET /api/pin/trang-thai/{trangThai}
```

## Tính năng Tự động Tạo Biến thể

Khi sử dụng API `POST /api/chi-tiet-san-pham/tao-bien-the`, hệ thống sẽ:

1. Lấy tất cả các thuộc tính đặc trưng được chọn
2. Tạo tất cả các tổ hợp có thể từ các thuộc tính này
3. Tự động tạo mã chi tiết sản phẩm cho mỗi biến thể
4. Lưu tất cả các biến thể vào database

**Ví dụ:** Nếu chọn 2 CPU, 2 GPU, 1 RAM, 2 ổ cứng, 2 màu sắc, 1 màn hình, 1 pin
→ Sẽ tạo ra 2 × 2 × 1 × 2 × 2 × 1 × 1 = 16 biến thể sản phẩm

## Validation

Tất cả các API đều có validation:
- Mã sản phẩm/chi tiết sản phẩm không được trùng
- Các trường bắt buộc không được để trống
- Giá bán phải >= 0
- Số lượng tồn phải >= 0

## Trạng thái

- `trangThai = 1`: Hoạt động
- `trangThai = 0`: Không hoạt động
- `trangThai = -1`: Đã xóa (soft delete)
