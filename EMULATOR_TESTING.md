# Hướng dẫn Test LAN Chat với Emulator

## 🎯 **Vấn đề với Emulator**

Emulator Android có network stack khác với thiết bị thật, nên cần cấu hình đặc biệt để LAN Chat hoạt động.

## 🚀 **Cách Test với 2 Emulator**

### Bước 1: Tạo 2 Emulator
1. Mở Android Studio → AVD Manager
2. Tạo 2 emulator khác nhau:
   - **Emulator 1**: API 30+ (Android 11+)
   - **Emulator 2**: API 30+ (Android 11+)
3. Đảm bảo cả 2 đều có Google Play Services

### Bước 2: Cấu hình Network
1. **Khởi động cả 2 emulator**
2. **Kiểm tra IP của từng emulator**:
   - Mở Terminal/Command Prompt
   - Chạy: `adb shell ip addr show`
   - Ghi nhớ IP của mỗi emulator

### Bước 3: Test Kết nối
1. **Cài đặt app trên cả 2 emulator**
2. **Mở app trên Emulator 1**:
   - Vào tab "Connections"
   - Nhấn "Scan for New Devices"
   - Chờ quá trình quét hoàn tất
3. **Mở app trên Emulator 2**:
   - Làm tương tự
4. **Kết nối**:
   - Trên Emulator 1, chọn Emulator 2 từ danh sách
   - Hoặc ngược lại

## 🔧 **Troubleshooting**

### Vấn đề 1: Không tìm thấy thiết bị
**Giải pháp**:
- Đảm bảo cả 2 emulator đang chạy
- Kiểm tra firewall Windows
- Thử restart emulator

### Vấn đề 2: Kết nối bị từ chối
**Giải pháp**:
- Kiểm tra logcat để xem lỗi chi tiết
- Đảm bảo app có quyền INTERNET
- Thử kết nối từ emulator khác

### Vấn đề 3: Tin nhắn không gửi được
**Giải pháp**:
- Kiểm tra trạng thái kết nối
- Đảm bảo cả 2 app đều đang mở
- Thử gửi tin nhắn ngắn trước

## 📱 **Test với Thiết bị thật**

### Bước 1: Cấu hình
1. **Kết nối cả 2 thiết bị cùng WiFi**
2. **Cài đặt app trên cả 2 thiết bị**
3. **Cấp quyền mạng cho app**

### Bước 2: Test
1. **Mở app trên thiết bị 1**
2. **Scan tìm thiết bị**
3. **Kết nối và chat**

## 🔍 **Debug Tips**

### Xem Logs
```bash
# Xem logs của emulator
adb logcat | grep NetworkService

# Xem logs của thiết bị thật
adb -s <device-id> logcat | grep NetworkService
```

### Kiểm tra Network
```bash
# Kiểm tra IP của emulator
adb shell ip addr show

# Ping test
adb shell ping 10.0.2.2
```

### Test Port
```bash
# Kiểm tra port 8888
adb shell netstat -tuln | grep 8888
```

## 📋 **Checklist Test**

- [ ] Cả 2 emulator đều chạy
- [ ] App cài đặt thành công
- [ ] Quyền mạng được cấp
- [ ] Scan tìm thấy thiết bị
- [ ] Kết nối thành công
- [ ] Gửi tin nhắn được
- [ ] Nhận tin nhắn được
- [ ] Disconnect hoạt động

## 🚨 **Lưu ý quan trọng**

1. **Emulator IP**: Thường là `10.0.2.15`, `10.0.2.16`, etc.
2. **Port**: 8888 (mặc định)
3. **Timeout**: 5 giây cho kết nối
4. **Firewall**: Tắt Windows Firewall nếu cần
5. **Network**: Đảm bảo emulator có internet

## 🎯 **Kết quả mong đợi**

- Emulator 1 hiển thị IP: `10.0.2.15`
- Emulator 2 hiển thị IP: `10.0.2.16`
- Scan tìm thấy cả 2 thiết bị
- Kết nối thành công
- Chat hoạt động bình thường

Nếu vẫn gặp vấn đề, hãy kiểm tra logs và đảm bảo cấu hình network đúng! 