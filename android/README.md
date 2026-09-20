# English AI Coach — Android Java V1

Module Android V1 dùng Java, XML/ViewBinding và MVVM theo baseline Android v1.1.

## Toolchain

- Android Gradle Plugin 8.13.2
- Gradle 8.13
- Gradle runtime: JDK 21
- Java source/target compatibility: Java 17
- `compileSdk` / `targetSdk` 36
- `minSdk` 26

JDK chạy Gradle và mức tương thích của source là hai khái niệm khác nhau:

- Gradle/Android Gradle Plugin của workflow được tài liệu hóa phải chạy bằng
  JDK 21.
- Source ứng dụng vẫn được biên dịch với
  `sourceCompatibility JavaVersion.VERSION_17` và
  `targetCompatibility JavaVersion.VERSION_17`.
- Không đổi source/target lên Java 21 chỉ vì Gradle chạy bằng JDK 21.
- Không dùng JDK 25 đang có trên `JAVA_HOME` hoặc đứng đầu `PATH` cho workflow
  Gradle được tài liệu hóa của repository này.

## Build và kiểm tra

Các lệnh bên dưới phải được chạy từ `<repository-root>\android`. Cài một bản
phân phối JDK 21 tương thích, ví dụ Eclipse Temurin 21, và Android SDK có
platform API 36, rồi mở PowerShell tại repository root. Chọn JDK 21 và Android
SDK chỉ cho PowerShell session hiện tại bằng flow portable sau; khi được hỏi,
nhập các thư mục cài đặt thực tế trên máy, không nhập nguyên một placeholder
hoặc đường dẫn của máy khác:

```powershell
$jdk21 = Read-Host "Nhập đường dẫn đầy đủ tới thư mục cài đặt JDK 21"
$jdk21Java = Join-Path $jdk21 "bin\java.exe"
if (-not (Test-Path -LiteralPath $jdk21Java -PathType Leaf)) {
    throw "Không tìm thấy bin\java.exe trong thư mục JDK 21 đã nhập."
}

$androidSdk = Read-Host "Nhập đường dẫn đầy đủ tới thư mục Android SDK"
$androidPlatform = Join-Path $androidSdk "platforms\android-36\android.jar"
if (-not (Test-Path -LiteralPath $androidPlatform -PathType Leaf)) {
    throw "Android SDK đã nhập không có platform API 36."
}

$env:JAVA_HOME = (Resolve-Path -LiteralPath $jdk21).Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME = (Resolve-Path -LiteralPath $androidSdk).Path
Set-Location .\android
```

Các thay đổi `JAVA_HOME`, `Path` và `ANDROID_HOME` trên chỉ tồn tại trong
PowerShell session hiện tại; không thay đổi cấu hình Java/Android SDK toàn cục
của máy và không cần tạo `local.properties`.

Kiểm tra cả Java trên shell và JVM thực tế mà Gradle wrapper đang sử dụng:

```powershell
java -version
.\gradlew.bat --version
```

`java -version` phải báo JDK 21. Quan trọng hơn, output của
`.\gradlew.bat --version` phải báo `Launcher JVM` và `Daemon JVM` dùng JDK 21.
Lệnh Gradle là nguồn kiểm tra authoritative vì Android Studio hoặc tooling
khác có thể chọn JVM khác với `java` trên `PATH`.

Sau khi xác nhận JDK 21, chạy canonical validation từ
`<repository-root>\android`:

```powershell
.\gradlew.bat --no-daemon clean lintDebug testDebugUnitTest assembleDebug assembleRelease
```

Cài bản debug lên emulator hoặc thiết bị đang kết nối:

```powershell
.\gradlew.bat --no-daemon installDebug
```

## Boundary của foundation

`AND-FND-001` chỉ dựng project, app shell và package boundary. Networking,
token/error/eventId thuộc `AND-FND-002`; navigation/design/connectivity baseline
thuộc `AND-FND-003`. Không thêm business feature, offline mutation queue hoặc
client-side learning algorithm vào foundation này.
