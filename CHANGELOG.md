# CHANGELOG

Tất cả các thay đổi đáng chú ý của dự án được ghi nhận tại đây.

---

## [1.0.0] - 2026-10-11

### Đã thêm (Added)
- **Cấu hình dự án (Project Infrastructure):**
  - Khởi tạo dự án Java 17 chuẩn với Gradle 8.7 (`build.gradle`, `settings.gradle`).
  - Thiết lập Gradle Wrapper tự động (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`).
  - Tích hợp JUnit 5 (`junit-jupiter:5.10.2`) và AssertJ (`assertj-core:3.25.3`).
  - Cấu hình `.gitignore` chuẩn cho các thư mục sinh ra khi build.

- **Bài 1 – Tính hóa đơn bán lẻ (`bai1`):**
  - `ProductCategory`: Enum thuế suất GTGT cho từng nhóm hàng (`THUC_PHAM`, `GIA_DUNG`, `DIEN_TU`, `SACH`).
  - `InvoiceLine`, `InvoiceLineDetail`, `InvoiceResult`: Triển khai bằng record Java 17.
  - `InvalidInvoiceException`: Xử lý ngoại lệ nghiệp vụ dữ liệu đầu vào không hợp lệ.
  - `InvoiceCalculator`: Logic tính toán hóa đơn, phân bổ giảm giá 2% cho đơn $\ge$ 2.000.000 đ, dồn phần lệch làm tròn vào dòng lớn nhất, tổng hợp thuế theo thuế suất.
  - `InvoiceCalculatorTest`: 8 ca kiểm thử bao phủ toàn bộ ví dụ mẫu và các trường hợp biên.

- **Bài 2 – Lịch trả nợ khoản vay tiêu dùng (`bai2`):**
  - `Method`: Enum phương thức trả nợ `GOC_DEU` và `TRA_DEU`.
  - `LoanRequest`, `Installment`, `LoanComparisonResult`: Record thông tin khoản vay và lịch trả nợ.
  - `InvalidLoanRequestException`: Ngoại lệ khi dữ liệu vay vi phạm quy định (tiền $\le$ 0, lãi $> 50\%$, kỳ ngoài 1–360).
  - `LoanScheduler`: Sinh lịch trả nợ theo cả 2 phương pháp, tự động chuyển ngày đến hạn nếu trùng Thứ Bảy/Chủ Nhật sang Thứ Hai kế tiếp, làm tròn `HALF_UP` đến đồng, dư nợ kỳ cuối bằng đúng 0.
  - `LoanSchedulerTest`: 4 ca kiểm thử xác thực khớp 100% bảng ví dụ đề thi (120M, 12%/năm, 6 kỳ) và các ngoại lệ.

- **Bài 3 – Báo cáo doanh số từ file CSV (`bai3`):**
  - `OrderStatus`: Enum trạng thái đơn hàng `COMPLETED`, `RETURNED`, `CANCELLED`.
  - `OrderRecord`: Record lưu thông tin đơn hàng và hàm tính doanh thu thuần.
  - `RowError`: Record ghi nhận dòng lỗi trong file CSV kèm số dòng và nguyên nhân.
  - `SalesReport`: Record tổng hợp kết quả báo cáo.
  - `SalesReporter`: Đọc file bằng `Files.lines` và tổng hợp bằng Stream API (không dùng vòng lặp).
  - `SalesReporterTest`: 1 ca kiểm thử toàn diện với tập dữ liệu CSV mẫu có 3 dòng lỗi (dòng 9, 12, 13) và đối chiếu kết quả từng vùng, top 3 sản phẩm, doanh thu theo tháng và tỷ lệ hoàn trả.

- **Bài 4 – Sổ cái chuyển tiền nội bộ (`bai4`):**
  - Hệ thống ngoại lệ: `InsufficientFundsException`, `DailyLimitExceededException`, `IdempotencyConflictException`, `AlreadyReversedException`, `AccountNotFoundException`, `InvalidTransactionException`.
  - `EntryType`: Enum loại ghi sổ `DEBIT` và `CREDIT`.
  - `JournalLine`, `JournalEntry`: Mô hình bút toán kép (bảo toàn Tổng Nợ = Tổng Có).
  - `Account`: Mô hình tài khoản ngân hàng không có setter cho số dư, kiểm tra hạn mức thấu chi và trần chuyển 500M/ngày.
  - `TransferRequest`, `TransferResult`, `StatementItem`, `AccountStatement`, `TrialBalance`: Các bản ghi DTO.
  - `LedgerService`: Triển khai chuyển tiền nội bộ, thu phí 0,1% (min 2k, max 50k), chống deadlock bằng sắp xếp Account ID, đảo bút toán (`reverse`), sao kê và bảng cân đối thử.
  - `LedgerServiceTest`: 4 ca kiểm thử bao gồm toàn bộ 6 bước kịch bản đề thi, kiểm tra trần phí, hạn mức ngày và kiểm thử tải đa luồng (50 tài khoản, 8 luồng, 20.000 giao dịch).

- **Bài 5 – Động cơ khuyến mại tối ưu cho giỏ hàng (`bai5`):**
  - `CustomerTier`, `Customer`: Hạng khách hàng (`BRONZE`, `SILVER`, `GOLD`, `PLATINUM`).
  - `CartItem`, `Cart`: Giỏ hàng và sản phẩm.
  - `Promotion`: Sealed interface cho hệ thống khuyến mại với 5 loại:
    - `BuyXGetYPromotion`
    - `CategoryPercentPromotion`
    - `BundlePromotion`
    - `OrderThresholdPromotion`
    - `VoucherPromotion`
  - `PromotionCandidate`: Đại diện một ứng viên áp dụng khuyến mại dòng.
  - `AppliedPromotion`, `PricingResult`: Kết quả tính giá kèm giải thích dạng văn bản.
  - `PromotionEngine`: Thuật toán Nhánh cận (Branch and Bound) tìm phương án tối ưu, tự động tránh bẫy tham lam giỏ hàng, áp dụng quy tắc hòa (ít khuyến mại dòng hơn, độ ưu tiên cao hơn).
  - `PromotionEngineTest`: 2 ca kiểm thử kiểm tra thời gian < 1s, thoát bẫy tham lam 170k về 123k, và lọc khuyến mại hết hạn/sai hạng khách.

- **Tài liệu & Giải đáp:**
  - `README.md`: Báo cáo kỹ thuật tổng quan, kiến trúc, hướng dẫn chạy test.
  - `EXAM_SOLUTIONS.md`: Đáp án và phân tích chi tiết toàn bộ 50 câu trắc nghiệm OCP Java 17 (1Z0-829) của Đề 1.

