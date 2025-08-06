# LAN Chat

Ứng dụng chat qua mạng LAN sử dụng TCP/IP sockets, cho phép các thiết bị trong cùng mạng WiFi có thể chat với nhau.

## Tính năng

- **Chat qua mạng LAN**: Kết nối và chat với các thiết bị khác trong cùng mạng WiFi
- **Tự động tìm kiếm thiết bị**: Quét mạng để tìm các thiết bị có thể kết nối
- **Giao diện hiện đại**: Material Design 3 với giao diện thân thiện
- **Hiển thị trạng thái kết nối**: Theo dõi trạng thái kết nối mạng
- **Gửi tin nhắn real-time**: Tin nhắn được gửi và nhận ngay lập tức

## Cách sử dụng

### Yêu cầu hệ thống
- Android 6.0 (API level 23) trở lên
- Thiết bị có WiFi
- Các thiết bị phải kết nối cùng mạng WiFi

### Hướng dẫn sử dụng

1. **Kết nối WiFi**: Đảm bảo thiết bị đã kết nối WiFi
2. **Mở ứng dụng**: Khởi động ứng dụng LAN Chat
3. **Tìm kiếm thiết bị**: 
   - Vào tab "Connections"
   - Nhấn "Scan for New Devices" để tìm thiết bị
   - Chờ quá trình quét hoàn tất
4. **Kết nối**: 
   - Chọn thiết bị từ danh sách
   - Ứng dụng sẽ tự động kết nối
5. **Chat**: 
   - Vào tab "Chat" để bắt đầu gửi tin nhắn
   - Tin nhắn sẽ được gửi đến tất cả thiết bị đã kết nối

### Kiến trúc kỹ thuật

- **NetworkService**: Xử lý kết nối TCP/IP, quản lý server và client
- **MainActivity**: Quản lý quyền mạng và khởi tạo NetworkService
- **ConnectionsFragment**: Hiển thị danh sách thiết bị và quản lý kết nối
- **ChatFragment**: Giao diện chat và gửi/nhận tin nhắn
- **NetworkDevicesAdapter**: Adapter cho danh sách thiết bị mạng

### Cấu hình mạng

- **Port mặc định**: 8888
- **Timeout kết nối**: 1000ms
- **Buffer size**: 1024 bytes
- **Quét mạng**: Tự động quét dải IP 1-254

## Quyền cần thiết

- `INTERNET`: Kết nối mạng
- `ACCESS_NETWORK_STATE`: Kiểm tra trạng thái mạng
- `ACCESS_WIFI_STATE`: Truy cập thông tin WiFi
- `CHANGE_WIFI_STATE`: Thay đổi cài đặt WiFi

## Công nghệ sử dụng

- **Kotlin**: Ngôn ngữ lập trình chính
- **TCP/IP Sockets**: Giao thức kết nối mạng
- **Coroutines**: Xử lý bất đồng bộ
- **Material Design 3**: Giao diện người dùng
- **View Binding**: Binding dữ liệu
- **Navigation Component**: Điều hướng

## Lưu ý

- Đảm bảo tất cả thiết bị đều kết nối cùng mạng WiFi
- Một số router có thể chặn kết nối giữa các thiết bị
- Ứng dụng hoạt động tốt nhất trong mạng LAN nội bộ
- Không hỗ trợ chat qua internet (chỉ mạng nội bộ)

## Phát triển

Để build và chạy ứng dụng:

```bash
# Clone repository
git clone <repository-url>

# Mở project trong Android Studio
# Sync Gradle
# Build và chạy trên thiết bị hoặc emulator
```

## License

MIT License 
