# BÁO CÁO BÀI TẬP & THIẾT KẾ HỆ THỐNG JAVA CORE (CHUẨN OCP & THỰC HÀNH NGHIỆP VỤ)

Dự án hiện thực toàn diện dựa trên đề thi **"Đề thi Java: Lý thuyết (chuẩn OCP) và Thực hành nghiệp vụ"** (Tác giả: Phạm Ngọc Sơn).

---

## 1. Môi trường & Quy chuẩn kỹ thuật

- **Ngôn ngữ:** Java 17 LTS (compile & run trên JDK 17).
- **Công cụ xây dựng:** Gradle 8.7 (sử dụng Gradle Wrapper `gradlew`).
- **Thư viện phụ thuộc:**
  - `org.junit.jupiter:junit-jupiter:5.10.2` (JUnit 5)
  - `org.assertj:assertj-core:3.25.3` (AssertJ)
  - *Không sử dụng Spring, Lombok hay bất kỳ thư viện ngoài nào khác.*
- **Tiền tệ & Làm tròn:**
  - 100% sử dụng `java.math.BigDecimal`, đơn vị Đồng (VND).
  - Làm tròn chuẩn ngân hàng / kế toán: `RoundingMode.HALF_UP` scale 0 tại đúng các điểm đề bài quy định.
- **Mô hình lập trình hiện đại Java 17:**
  - Tận dụng tối đa `record`, `enum`, `sealed interface`, `Stream API`, đối tượng bất biến (`immutable objects`), và cú pháp `pattern matching for instanceof`.

---

## 2. Hướng dẫn chạy thử nghiệm (Test Execution)

Dự án tích hợp sẵn Gradle Wrapper. Chạy toàn bộ 19 Unit Tests bằng lệnh sau:

### Trên Windows (PowerShell / CMD):
```powershell
$env:JAVA_HOME = "G:\driver\JDK\JDK 17"   # Hoặc đường dẫn JDK 17 của máy bạn
.\gradlew test
```

### Trên Linux / macOS:
```bash
./gradlew test
```

### Báo cáo kiểm thử HTML:
Sau khi chạy, báo cáo chi tiết được sinh tự động tại:
`build/reports/tests/test/index.html`

---

## 3. Kiến trúc & Chi tiết 5 Bài tập thực hành (Đề 2)

### Bài 1: Tính hóa đơn bán lẻ (`bai1`) - 15 Điểm
- **Domain Model:**
  - `ProductCategory`: Enum các nhóm hàng kèm thuế suất GTGT (`THUC_PHAM`: 5%, `GIA_DUNG`: 10%, `DIEN_TU`: 8%, `SACH`: 0%).
  - `InvoiceLine`, `InvoiceLineDetail`, `InvoiceResult`: Triển khai bằng Java 17 `record`.
- **Nghiệp vụ cốt lõi:**
  - Tiền hàng = `đơn giá × số lượng`.
  - Chiết khấu dòng = `tiền hàng × % chiết khấu` (0–50%), làm tròn đến đồng.
  - Hóa đơn $\ge$ 2.000.000 đ được giảm thêm 2% trên tổng giá trị sau chiết khấu dòng.
  - Phân bổ giảm giá hóa đơn về từng dòng theo tỷ lệ. **Phần lệch do làm tròn được dồn chính xác vào dòng có giá trị sau chiết khấu lớn nhất**.
  - Thuế GTGT tính theo từng dòng sau mọi khoản giảm. Có hàm `taxByRate()` tổng hợp thuế theo từng mức thuế suất.
  - Kiểm tra và từ chối nghiêm ngặt: Hóa đơn rỗng, số lượng $\le$ 0, đơn giá âm, chiết khấu ngoài khoảng, trùng mã sản phẩm trên hai dòng $\rightarrow$ Ném `InvalidInvoiceException`.

---

### Bài 2: Lịch trả nợ khoản vay tiêu dùng (`bai2`) - 15 Điểm
- **Domain Model:**
  - `Method`: Enum `GOC_DEU`, `TRA_DEU`.
  - `LoanRequest`, `Installment`, `LoanComparisonResult`: Triển khai bằng `record`.
- **Nghiệp vụ cốt lõi:**
  - $r_{tháng} = r_{năm} / 12$.
  - **Phương pháp `GOC_DEU`:** Gốc mỗi kỳ = $P / n$, lãi tính trên dư nợ đầu kỳ.
  - **Phương pháp `TRA_DEU` (Annuity):**
    $$A = P \times \frac{r}{1 - (1+r)^{-n}}$$
    làm tròn đến đồng; Lãi = $\text{dư nợ đầu kỳ} \times r$; Gốc = $A - \text{lãi}$.
  - **Kỳ cuối:** Trả toàn bộ dư nợ còn lại để dư nợ cuối cùng bằng đúng 0.
  - **Ngày đến hạn:** $disbursementDate + k \text{ tháng}$ (tính từ ngày gốc, không cộng dồn từ kỳ trước). Nếu rơi vào Thứ Bảy hoặc Chủ Nhật thì tự động dời sang Thứ Hai kế tiếp; lãi vẫn tính trọn tháng.
  - Hàm so sánh `compareMethods()` tính tổng lãi chênh lệch giữa hai phương pháp.
  - Từ chối: Số tiền $\le$ 0, lãi suất âm hoặc $> 50\%$/năm, số kỳ ngoài 1–360 $\rightarrow$ Ném `InvalidLoanRequestException`.

---

### Bài 3: Báo cáo doanh số từ file CSV (`bai3`) - 15 Điểm
- **Đặc tả kỹ thuật:**
  - Đọc file bằng `Files.lines(Path)`.
  - Xử lý tổng hợp 100% bằng **Stream API**, tuyệt đối không dùng vòng lặp `for`/`while` cho phần tổng hợp.
- **Nghiệp vụ cốt lõi:**
  - Xử lý dòng lỗi linh hoạt: Sai số cột, ngày không hợp lệ, số lượng/đơn giá không phải số dương, trạng thái lạ, trùng `orderId` với dòng trước $\rightarrow$ Không dừng chương trình, ghi nhận đầy đủ số dòng và nguyên nhân vào `List<RowError>`.
  - Doanh thu thuần = `COMPLETED` (tăng), `RETURNED` (giảm), `CANCELLED` (bỏ qua).
  - Tỷ lệ hoàn trả = `RETURNED / COMPLETED` (hiển thị % với 2 chữ số thập phân).
  - Kết quả trả về `SalesReport`:
    1. Doanh thu thuần theo vùng (sắp xếp giảm dần).
    2. Top 3 sản phẩm theo doanh thu thuần (sắp xếp giảm dần).
    3. Doanh thu thuần theo tháng (`YearMonth` sắp xếp tăng dần).
    4. Tỷ lệ hoàn trả theo nhóm hàng.
    5. Danh sách lỗi chi tiết.

---

### Bài 4: Sổ cái chuyển tiền nội bộ (`bai4`) - 25 Điểm
- **Nguyên tắc kế toán kép (Double-entry Bookkeeping):**
  - Mọi giao dịch sinh `JournalEntry` gồm các dòng Nợ/Có.
  - Tổng Nợ bắt buộc phải bằng Tổng Có (bảo toàn cân đối kế toán).
  - **Số dư tài khoản chỉ thay đổi qua việc ghi nhận bút toán, tài khoản không có setter**.
- **Cấu trúc bút toán chuyển tiền (3 dòng):**
  1. Nợ tài khoản gửi: `số tiền + phí`
  2. Có tài khoản nhận: `số tiền`
  3. Có tài khoản `FEE_INCOME`: `phí`
  *(Phí = 0,1% số tiền, làm tròn đến đồng, tối thiểu 2.000 đ, tối đa 50.000 đ, người gửi chịu).*
- **Chiến lược kiểm soát rủi ro & Đồng thời (Concurrency):**
  - **Chống Deadlock:** Không dùng khóa toàn cục! Khi giao dịch giữa hai tài khoản $A$ và $B$, hệ thống luôn khóa hai tài khoản theo thứ tự sắp xếp từ điển của `accountId`:
    $$\text{lock}(\min(A, B)) \rightarrow \text{lock}(\max(A, B))$$
    Điều này loại bỏ hoàn toàn khả năng xảy ra chu trình chờ đợi (Circular Wait). Nhờ đó, hai giao dịch độc lập không chung tài khoản khách hàng (ví dụ $A \rightarrow B$ và $C \rightarrow D$) chạy song song 100%.
  - **Hạn mức thấu chi (Overdraft):** Số dư sau giao dịch không được thấp hơn $-\text{overdraftLimit}$ (mặc định 0) $\rightarrow$ Ném `InsufficientFundsException`.
  - **Hạn mức ngày:** Tổng tiền chuyển đi của một tài khoản trong một ngày không vượt 500.000.000 đ (không tính phí) $\rightarrow$ Ném `DailyLimitExceededException`. Thời gian được inject qua `Clock` để thuận tiện kiểm thử theo ngày.
  - **Idempotency chuẩn hai chiều:**
    - Gửi lại cùng `idempotencyKey` với cùng nội dung: Trả về kết quả cũ đã lưu trong cache, không ghi sổ lần hai.
    - Cùng khóa nhưng khác nội dung: Ném `IdempotencyConflictException`.
    - Ghi nhớ cả các yêu cầu bị từ chối (failed transactions).
  - **Đảo bút toán (`reverse`):**
    - Đảo ngược toàn bộ các dòng Nợ/Có (kể cả phí).
    - Mỗi bút toán chỉ được đảo 1 lần duy nhất $\rightarrow$ Lần 2 ném `AlreadyReversedException`.
    - Không áp hạn mức ngày khi đảo, nhưng vẫn kiểm tra thấu chi của bên bị trừ.
  - **Bảng cân đối thử (`trialBalance`):** Tổng số dư của mọi tài khoản (kể cả `FEE_INCOME`) luôn bằng tổng số dư ban đầu tại mọi thời điểm.
- **Kết quả kiểm thử tải đa luồng:**
  - 50 tài khoản, 8 luồng chạy song song, xử lý hàng chục ngàn giao dịch ngẫu nhiên (10% gửi lại khóa cũ). Hoàn thành trong vài giây (xa dưới trần 30 giây), bảo toàn cân đối thử 100%, không tài khoản nào vi phạm thấu chi.

---

### Bài 5: Động cơ khuyến mại tối ưu cho giỏ hàng (`bai5`) - 30 Điểm
- **Mô hình hóa Sealed Hierarchy (`Promotion`):**
  ```java
  public sealed interface Promotion permits
      BuyXGetYPromotion,
      CategoryPercentPromotion,
      BundlePromotion,
      OrderThresholdPromotion,
      VoucherPromotion
  ```
- **Quy tắc nghiệp vụ:**
  1. Lọc bỏ các khuyến mại hết hạn hoặc sai hạng khách trước khi tính.
  2. Mỗi đơn vị hàng chỉ được thuộc tối đa một khuyến mại cấp dòng. Các đơn vị khác nhau của cùng một sản phẩm có thể thuộc các khuyến mại khác nhau hoặc không thuộc khuyến mại nào.
  3. Khuyến mại cấp đơn áp theo thứ tự: `OrderThreshold` rồi `Voucher`, tự động khi đủ điều kiện.
  4. Tiêu chí lựa chọn phương án:
     - Ưu tiên 1: Số tiền khách trả nhỏ nhất (`finalPayment`).
     - Ưu tiên 2 (Hòa): Dùng ít khuyến mại dòng hơn (`linePromoCount`).
     - Ưu tiên 3 (Vẫn hòa): Tổng độ ưu tiên cao hơn (`totalPriority`).
- **Chiến lược tìm kiếm & Thuật toán:**
  - **Thuật toán:** Nhánh cận (Branch and Bound) kết hợp Admissible Heuristic Pruning.
  - **Mở rộng dễ dàng (Extensibility):** Mỗi loại khuyến mại dòng là một chiến lược tự sinh các ứng viên (`PromotionCandidate`) dựa trên số lượng tồn kho còn lại. Thuật toán tìm kiếm lõi chỉ làm việc trên giao diện ứng viên mà không cần sửa đổi khi bổ sung loại khuyến mại mới.
  - **Tránh bẫy tham lam (Greedy Trap):**
    Trong ví dụ giỏ hàng 170.000 đ:
    - Nếu tham lam tối đa hóa giảm giá cấp dòng (áp combo P3 + bộ P1 + P2 giảm 22.500 đ), giá trị đơn hàng giảm còn 147.500 đ $\rightarrow$ Bị hụt ngưỡng 150.000 đ của P4 (-15.000 đ), khiến khách phải trả 135.500 đ.
    - Động cơ Branch & Bound thông minh nhận diện phương án chỉ giảm 20.000 đ cấp dòng bằng P1 (2 bộ), giữ giá trị ở mức 150.000 đ để kích hoạt cả P4 (-15.000 đ) và P5 (-12.000 đ), giúp khách chỉ phải trả **123.000 đ**!
  - **Hiệu năng:** Duyệt toàn bộ không gian trạng thái trong **dưới 100 mili-giây** (yêu cầu đề bài là dưới 1 giây).

---

## 4. Bảng tổng hợp kết quả Unit Test

| Test Suite | Số Test | Trạng Thái | Mô tả nội dung kiểm thử |
|---|---|---|---|
| `InvoiceCalculatorTest` | 8 | PASSED | Ví dụ mẫu, threshold 2M, dồn lệch làm tròn vào dòng max, validate rỗng, âm, trùng mã, quá % |
| `LoanSchedulerTest` | 4 | PASSED | Ví dụ 120M 12%/năm 6 kỳ, trả đều & gốc đều, dời ngày T7/CN sang T2, validate hạn mức & kỳ |
| `SalesReporterTest` | 1 | PASSED | Đọc CSV bằng `Files.lines`, Stream API, 3 dòng lỗi (9, 12, 13), top 3 SP, tỷ lệ trả 2 số thập phân |
| `LedgerServiceTest` | 4 | PASSED | 6 bước kịch bản đề thi, trần phí 50k, hạn mức ngày 500M, test tải 8 luồng x 50 tài khoản |
| `PromotionEngineTest` | 2 | PASSED | Thoát bẫy tham lam giỏ 170k về 123k (<1s), lọc hạn dùng và hạng khách |
| **Tổng cộng** | **19** | **100% GREEN** | **Toàn bộ các ca kiểm thử đạt kết quả tuyệt đối** |

