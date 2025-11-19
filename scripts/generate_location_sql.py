#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script tự động generate file SQL insert dữ liệu địa lý Việt Nam từ API
Sử dụng API: https://provinces.open-api.vn/

Cách chạy:
    python generate_location_sql.py

Output: location_data_full.sql
"""

import requests
import json
from datetime import datetime

API_BASE_URL = "https://provinces.open-api.vn/api"

def fetch_provinces():
    """Lấy danh sách tất cả tỉnh/thành phố"""
    url = f"{API_BASE_URL}/p/"
    print(f"📡 Đang fetch dữ liệu từ: {url}")
    response = requests.get(url)
    response.raise_for_status()
    return response.json()

def fetch_districts(province_code):
    """Lấy danh sách huyện/quận của một tỉnh"""
    url = f"{API_BASE_URL}/p/{province_code}?depth=2"
    response = requests.get(url)
    response.raise_for_status()
    return response.json().get('districts', [])

def fetch_wards(district_code):
    """Lấy danh sách xã/phường của một huyện"""
    url = f"{API_BASE_URL}/d/{district_code}?depth=2"
    response = requests.get(url)
    response.raise_for_status()
    return response.json().get('wards', [])

def generate_sql():
    """Generate file SQL từ dữ liệu API"""
    
    print("🔄 Bắt đầu generate SQL...")
    
    sql_statements = []
    sql_statements.append("-- ===================================================================================")
    sql_statements.append("-- FILE SQL TỰ ĐỘNG GENERATE TỪ API provinces.open-api.vn")
    sql_statements.append(f"-- Generated at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    sql_statements.append("-- ===================================================================================")
    sql_statements.append("")
    sql_statements.append("USE QuanLyBanHangLaptop_TheoERD1;")
    sql_statements.append("GO")
    sql_statements.append("")
    
    # Fetch provinces
    provinces = fetch_provinces()
    print(f"✅ Đã lấy {len(provinces)} tỉnh/thành phố")
    
    # Insert provinces
    sql_statements.append("-- ===================================================================================")
    sql_statements.append("-- INSERT TỈNH/THÀNH PHỐ")
    sql_statements.append("-- ===================================================================================")
    sql_statements.append("")
    
    province_count = 0
    district_count = 0
    ward_count = 0
    
    for province in provinces:
        province_code = province.get('code', '')
        province_name = province.get('name', '').replace("'", "''")  # Escape single quote
        province_id = province.get('code', '')
        
        sql = f"INSERT INTO lc_province (id, name, shortname, code, country_id, created_date) VALUES ({province_id}, N'{province_name}', NULL, '{province_code}', 1, GETDATE());"
        sql_statements.append(sql)
        province_count += 1
    
    sql_statements.append("")
    sql_statements.append(f"PRINT 'Đã insert {province_count} tỉnh/thành phố';")
    sql_statements.append("GO")
    sql_statements.append("")
    
    # Insert districts and wards
    sql_statements.append("-- ===================================================================================")
    sql_statements.append("-- INSERT HUYỆN/QUẬN VÀ XÃ/PHƯỜNG")
    sql_statements.append("-- ===================================================================================")
    sql_statements.append("")
    
    for idx, province in enumerate(provinces, 1):
        province_code = province.get('code', '')
        province_name = province.get('name', '')
        
        print(f"📦 Đang xử lý {idx}/{len(provinces)}: {province_name}...")
        
        try:
            districts = fetch_districts(province_code)
            
            for district in districts:
                district_code = district.get('code', '')
                district_name = district.get('name', '').replace("'", "''")
                district_id = district.get('code', '')
                
                sql = f"INSERT INTO lc_district (id, province_id, province_code, name, shortname, code, created_date) VALUES ({district_id}, {province_code}, '{province_code}', N'{district_name}', NULL, '{district_code}', GETDATE());"
                sql_statements.append(sql)
                district_count += 1
                
                # Fetch wards for this district
                try:
                    wards = fetch_wards(district_code)
                    
                    for ward in wards:
                        ward_code = ward.get('code', '')
                        ward_name = ward.get('name', '').replace("'", "''")
                        ward_id = ward.get('code', '')
                        
                        sql = f"INSERT INTO lc_subdistrict (id, district_id, district_code, province_id, province_code, name, shortname, code, created_date) VALUES ({ward_id}, {district_code}, '{district_code}', {province_code}, '{province_code}', N'{ward_name}', NULL, '{ward_code}', GETDATE());"
                        sql_statements.append(sql)
                        ward_count += 1
                        
                except Exception as e:
                    print(f"  ⚠️  Lỗi khi lấy xã/phường cho huyện {district_name}: {e}")
                    continue
                    
        except Exception as e:
            print(f"  ⚠️  Lỗi khi lấy huyện/quận cho tỉnh {province_name}: {e}")
            continue
    
    sql_statements.append("")
    sql_statements.append(f"PRINT 'Đã insert {district_count} huyện/quận';")
    sql_statements.append(f"PRINT 'Đã insert {ward_count} xã/phường';")
    sql_statements.append("GO")
    sql_statements.append("")
    sql_statements.append("PRINT '===================================================================================';")
    sql_statements.append("PRINT 'HOÀN THÀNH: Đã insert dữ liệu địa lý đầy đủ';")
    sql_statements.append(f"PRINT 'Tổng số: {province_count} tỉnh, {district_count} huyện, {ward_count} xã';")
    sql_statements.append("PRINT '===================================================================================';")
    sql_statements.append("GO")
    
    # Write to file
    output_file = "location_data_full.sql"
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(sql_statements))
    
    print(f"\n✅ Hoàn thành!")
    print(f"📄 File output: {output_file}")
    print(f"📊 Thống kê:")
    print(f"   - Tỉnh/thành phố: {province_count}")
    print(f"   - Huyện/quận: {district_count}")
    print(f"   - Xã/phường: {ward_count}")
    print(f"\n💡 Chạy file SQL bằng lệnh:")
    print(f"   sqlcmd -S localhost,1433 -d QuanLyBanHangLaptop_TheoERD1 -U sa -P 12345678 -i {output_file}")

if __name__ == "__main__":
    try:
        generate_sql()
    except Exception as e:
        print(f"❌ Lỗi: {e}")
        import traceback
        traceback.print_exc()

