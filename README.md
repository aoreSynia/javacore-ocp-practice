# Báo cáo bài tập Java Core

Dự án triển khai các bài thực hành trong đề **"Đề thi Java: Lý thuyết (chuẩn OCP) và Thực hành nghiệp vụ"** (Tác giả: Phạm Ngọc Sơn).

---

## 1. Môi trường và quy ước kỹ thuật

- **Ngôn ngữ:** Java 17 LTS (compile & run trên JDK 17).
- **Công cụ build:** Gradle 8.7, chạy qua Gradle Wrapper `gradlew`.
- **Thư viện phụ thuộc:**
  - `org.junit.jupiter:junit-jupiter:5.10.2` (JUnit 5)
  - `org.assertj:assertj-core:3.25.3` (AssertJ)
  - *Không sử dụng Spring, Lombok hay bất kỳ thư viện ngoài nào khác.*
- **Tiền tệ & Làm tròn:**
  - Dùng `java.math.BigDecimal`, đơn vị Đồng (VND).
  - Làm tròn bằng `RoundingMode.HALF_UP` scale 0 tại các điểm đề bài quy định.
- **Java 17:**
  - Dùng `record`, `enum`, `sealed interface`, `Stream API`, immutable object và `pattern matching for instanceof` ở các chỗ phù hợp.

---

## 2. Hướng dẫn chạy thử nghiệm (Test Execution)

Dự án có sẵn Gradle Wrapper. Chạy toàn bộ 19 unit test bằng lệnh sau:

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
Sau khi chạy, báo cáo HTML nằm tại:
`build/reports/tests/test/index.html`

---

## 3. Năm bài thực hành

### Bài 1: Tính hóa đơn bán lẻ (`bai1`) - 15 Điểm
- **Domain Model:**
  - `ProductCategory`: Enum các nhóm hàng kèm thuế suất GTGT (`THUC_PHAM`: 5%, `GIA_DUNG`: 10%, `DIEN_TU`: 8%, `SACH`: 0%).
  - `InvoiceLine`, `InvoiceLineDetail`, `InvoiceResult`: Triển khai bằng Java 17 `record`.
- **Nghiệp vụ chính:**
  - Tiền hàng = `đơn giá × số lượng`.
  - Chiết khấu dòng = `tiền hàng × % chiết khấu` (0–50%), làm tròn đến đồng.
  - Hóa đơn $\ge$ 2.000.000 đ được giảm thêm 2% trên tổng giá trị sau chiết khấu dòng.
  - Phân bổ giảm giá hóa đơn về từng dòng theo tỷ lệ. Phần lệch do làm tròn được dồn vào dòng có giá trị sau chiết khấu lớn nhất.
  - Thuế GTGT tính theo từng dòng sau mọi khoản giảm. Có hàm `taxByRate()` tổng hợp thuế theo từng mức thuế suất.
  - Từ chối hóa đơn rỗng, số lượng $\le$ 0, đơn giá âm, chiết khấu ngoài khoảng và mã sản phẩm bị trùng bằng `InvalidInvoiceException`.

---

### Bài 2: Lịch trả nợ khoản vay tiêu dùng (`bai2`) - 15 Điểm
- **Domain Model:**
  - `Method`: Enum `GOC_DEU`, `TRA_DEU`.
  - `LoanRequest`, `Installment`, `LoanComparisonResult`: Triển khai bằng `record`.
- **Nghiệp vụ chính:**
  - $r_{tháng} = r_{năm} / 12$.
  - **Phương pháp `GOC_DEU`:** Gốc mỗi kỳ = $P / n$, lãi tính trên dư nợ đầu kỳ.
  - **Phương pháp `TRA_DEU` (Annuity):**
    $$A = P \times \frac{r}{1 - (1+r)^{-n}}$$
    làm tròn đến đồng; Lãi = $\text{dư nợ đầu kỳ} \times r$; Gốc = $A - \text{lãi}$.
  - **Kỳ cuối:** Trả toàn bộ dư nợ còn lại để dư nợ cuối cùng bằng đúng 0.
  - **Ngày đến hạn:** $disbursementDate + k \text{ tháng}$ (tính từ ngày gốc, không cộng dồn từ kỳ trước). Nếu rơi vào Thứ Bảy hoặc Chủ Nhật thì dời sang Thứ Hai kế tiếp; lãi vẫn tính trọn tháng.
  - Hàm so sánh `compareMethods()` tính tổng lãi chênh lệch giữa hai phương pháp.
  - Từ chối: Số tiền $\le$ 0, lãi suất âm hoặc $> 50\%$/năm, số kỳ ngoài 1–360 $\rightarrow$ Ném `InvalidLoanRequestException`.

---

### Bài 3: Báo cáo doanh số từ file CSV (`bai3`) - 15 Điểm
- **Đặc tả kỹ thuật:**
  - Đọc file bằng `Files.lines(Path)`.
  - Phần tổng hợp dùng **Stream API**, không dùng vòng lặp `for`/`while`.
- **Nghiệp vụ chính:**
  - Dòng lỗi không làm dừng chương trình. Các lỗi sai số cột, ngày không hợp lệ, số lượng/đơn giá không phải số dương, trạng thái lạ và trùng `orderId` được ghi vào `List<RowError>`.
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
    Cách khóa này tránh chu trình chờ đợi (Circular Wait). Hai giao dịch không chung tài khoản khách hàng (ví dụ $A \rightarrow B$ và $C \rightarrow D$) vẫn có thể chạy song song.
  - **Hạn mức thấu chi (Overdraft):** Số dư sau giao dịch không được thấp hơn $-\text{overdraftLimit}$ (mặc định 0) $\rightarrow$ Ném `InsufficientFundsException`.
  - **Hạn mức ngày:** Tổng tiền chuyển đi của một tài khoản trong một ngày không vượt 500.000.000 đ (không tính phí) $\rightarrow$ Ném `DailyLimitExceededException`. Thời gian được inject qua `Clock` để thuận tiện kiểm thử theo ngày.
  - **Idempotency:**
    - Gửi lại cùng `idempotencyKey` với cùng nội dung: Trả về kết quả cũ đã lưu trong cache, không ghi sổ lần hai.
    - Cùng khóa nhưng khác nội dung: Ném `IdempotencyConflictException`.
    - Ghi nhớ cả các yêu cầu bị từ chối (failed transactions).
  - **Đảo bút toán (`reverse`):**
    - Đảo ngược toàn bộ các dòng Nợ/Có (kể cả phí).
    - Mỗi bút toán chỉ được đảo 1 lần duy nhất $\rightarrow$ Lần 2 ném `AlreadyReversedException`.
    - Không áp hạn mức ngày khi đảo, nhưng vẫn kiểm tra thấu chi của bên bị trừ.
  - **Bảng cân đối thử (`trialBalance`):** Tổng số dư của mọi tài khoản (kể cả `FEE_INCOME`) luôn bằng tổng số dư ban đầu tại mọi thời điểm.
- **Kết quả kiểm thử tải đa luồng:**
  - 50 tài khoản, 8 luồng chạy song song, 20.000 giao dịch ngẫu nhiên (10% gửi lại khóa cũ). Test hoàn thành dưới ngưỡng 30 giây, cân đối thử không đổi và không tài khoản nào vượt hạn mức thấu chi.

---

### Bài 5: Động cơ khuyến mại cho giỏ hàng (`bai5`) - 30 Điểm
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
     - Ưu tiên 1: Số tiền khách trả thấp nhất (`finalPayment`).
     - Ưu tiên 2 (Hòa): Dùng ít khuyến mại dòng hơn (`linePromoCount`).
     - Ưu tiên 3 (Vẫn hòa): Tổng độ ưu tiên cao hơn (`totalPriority`).
- **Chiến lược tìm kiếm & Thuật toán:**
  - **Thuật toán:** Nhánh cận (Branch and Bound) kết hợp Admissible Heuristic Pruning.
  - **Mở rộng:** Mỗi loại khuyến mại dòng tự sinh các ứng viên (`PromotionCandidate`) dựa trên số lượng tồn kho còn lại. Phần tìm kiếm làm việc với danh sách ứng viên, nên có thể bổ sung loại khuyến mại mới mà ít ảnh hưởng tới lõi thuật toán.
  - **Tránh bẫy tham lam (Greedy Trap):**
    Trong ví dụ giỏ hàng 170.000 đ:
    - Nếu tham lam tối đa hóa giảm giá cấp dòng (áp combo P3 + bộ P1 + P2 giảm 22.500 đ), giá trị đơn hàng giảm còn 147.500 đ $\rightarrow$ Bị hụt ngưỡng 150.000 đ của P4 (-15.000 đ), khiến khách phải trả 135.500 đ.
    - Branch & Bound chọn phương án chỉ giảm 20.000 đ cấp dòng bằng P1 (2 bộ), giữ giá trị ở mức 150.000 đ để kích hoạt cả P4 (-15.000 đ) và P5 (-12.000 đ). Kết quả khách trả **123.000 đ**.
  - **Hiệu năng:** Test kiểm tra thời gian xử lý dưới 1 giây theo yêu cầu đề bài.

---

## 4. Bảng tổng hợp kết quả Unit Test

| Test Suite | Số Test | Trạng Thái | Mô tả nội dung kiểm thử |
|---|---|---|---|
| `InvoiceCalculatorTest` | 8 | PASSED | Ví dụ mẫu, threshold 2M, dồn lệch làm tròn vào dòng max, validate rỗng, âm, trùng mã, quá % |
| `LoanSchedulerTest` | 4 | PASSED | Ví dụ 120M 12%/năm 6 kỳ, trả đều & gốc đều, dời ngày T7/CN sang T2, validate hạn mức & kỳ |
| `SalesReporterTest` | 1 | PASSED | Đọc CSV bằng `Files.lines`, Stream API, 3 dòng lỗi (9, 12, 13), top 3 SP, tỷ lệ trả 2 số thập phân |
| `LedgerServiceTest` | 4 | PASSED | 6 bước kịch bản đề thi, trần phí 50k, hạn mức ngày 500M, test tải 8 luồng x 50 tài khoản |
| `PromotionEngineTest` | 2 | PASSED | Thoát bẫy tham lam giỏ 170k về 123k (<1s), lọc hạn dùng và hạng khách |
| **Tổng cộng** | **19** | **PASSED** | Các test hiện có đều đạt |

