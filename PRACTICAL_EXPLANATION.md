# Giải thích phần thực hành theo file JAVACORE.pdf

Tài liệu này giải thích 5 bài thực hành trong **`[JAVACORE]Đề thi Java Lý thuyết (chuẩn OCP) và Thực hành nghiệp vụ.pdf`**, phần **Đề 2 - Thực hành**. Cách giải thích đi theo thứ tự: bối cảnh trong đề, mindset xử lý, cách giải, rồi cách tổ chức code trong dự án.

## Quy định chung từ đề

Đề yêu cầu làm bằng Java 17 thuần, nộp dưới dạng Git repo build được bằng Maven hoặc Gradle. Dự án này chọn Gradle.

Các ràng buộc quan trọng:

- Chỉ dùng JDK 17, JUnit 5 và AssertJ; không dùng Spring, Lombok hoặc thư viện nghiệp vụ khác.
- Tiền tệ dùng `BigDecimal`, đơn vị VND, làm tròn đến đồng bằng `RoundingMode.HALF_UP`.
- Không dùng `double` cho tiền.
- Mỗi bài đặt trong một package riêng từ `bai1` đến `bai5`.
- Dữ liệu không hợp lệ phải ném exception nghiệp vụ có tên rõ ràng, không trả `null`, không chỉ in lỗi ra console.
- Ưu tiên `record`, `enum`, `sealed interface`, Stream API và object bất biến khi phù hợp.
- README cần nêu giả định, quyết định thiết kế và phần chưa hoàn thành.

Mindset chung: trước khi code, phải tách bài toán thành dữ liệu đầu vào, luật nghiệp vụ, dữ liệu đầu ra, lỗi nghiệp vụ và test mẫu. Khi đã tách được như vậy, code sẽ tự nhiên chia thành `record` giữ dữ liệu, `enum` giữ tập giá trị cố định, exception cho lỗi, và service chính xử lý nghiệp vụ.

---

## Bài 1 - Tính hóa đơn bán lẻ (`bai1`)

### Đề bài yêu cầu gì

Chuỗi siêu thị cần module tính tiền cho hóa đơn có nhiều dòng hàng. Mỗi nhóm hàng có thuế GTGT khác nhau:

- `THUC_PHAM`: 5%
- `GIA_DUNG`: 10%
- `DIEN_TU`: 8%
- `SACH`: 0%

Quy tắc chính:

- Tiền hàng dòng = đơn giá x số lượng.
- Chiết khấu dòng = tiền hàng x phần trăm chiết khấu dòng, trong khoảng 0-50%.
- Nếu tổng sau chiết khấu dòng từ 2.000.000 đ trở lên thì giảm thêm 2% toàn hóa đơn.
- Giảm giá hóa đơn được phân bổ về từng dòng theo tỷ lệ giá trị sau chiết khấu dòng.
- Phần lệch làm tròn được dồn vào dòng có giá trị lớn nhất.
- VAT tính trên giá trị sau mọi khoản giảm.
- Từ chối hóa đơn rỗng, số lượng <= 0, đơn giá âm, chiết khấu sai khoảng, hoặc trùng mã sản phẩm.

Ví dụ trong PDF có kết quả tổng thanh toán **3.140.116 đ**.

### Mindset

Bài này là bài tiền tệ, nên ưu tiên đúng số hơn là viết ngắn. Chỗ dễ sai không nằm ở phép nhân đơn giản, mà ở thứ tự tính và làm tròn:

1. Tính từng dòng trước.
2. Cộng tổng sau chiết khấu dòng.
3. Tính giảm giá cấp hóa đơn nếu đủ ngưỡng.
4. Phân bổ giảm giá cấp hóa đơn ngược về từng dòng.
5. Tính VAT sau khi đã phân bổ giảm giá.

Không nên tính VAT trên tổng hóa đơn, vì đề yêu cầu VAT theo từng dòng và từng thuế suất.

### Cách giải

`InvoiceCalculator.calculate(List<InvoiceLine>)` nên làm theo luồng:

1. Validate toàn bộ input.
2. Tính `lineAmount`, `lineDiscount`, `afterLineDiscount` cho từng dòng.
3. Tính tổng sau chiết khấu dòng.
4. Nếu đạt ngưỡng 2.000.000 đ, tính giảm giá hóa đơn 2%.
5. Phân bổ giảm giá hóa đơn theo tỷ lệ từng dòng.
6. Chỉnh phần lệch làm tròn vào dòng lớn nhất.
7. Tính taxable amount và VAT từng dòng.
8. Trả về `InvoiceResult`.

### Tổ chức code

- `ProductCategory`: enum nhóm hàng và thuế suất.
- `InvoiceLine`: dữ liệu đầu vào một dòng hàng.
- `InvoiceLineDetail`: kết quả tính toán của từng dòng.
- `InvoiceResult`: tổng hợp kết quả hóa đơn.
- `InvoiceCalculator`: service xử lý nghiệp vụ.
- `InvalidInvoiceException`: lỗi input hóa đơn.

Test nên bám ví dụ PDF, case dưới ngưỡng 2.000.000 đ, case làm tròn phân bổ, và các input lỗi.

---

## Bài 2 - Lịch trả nợ khoản vay tiêu dùng (`bai2`)

### Đề bài yêu cầu gì

Công ty tài chính cần sinh lịch trả nợ hàng tháng theo 2 phương pháp để khách hàng so sánh:

- `GOC_DEU`: gốc mỗi kỳ bằng số tiền vay / số kỳ.
- `TRA_DEU`: trả đều theo công thức annuity.

Input gồm số tiền vay, lãi suất năm, số kỳ theo tháng và ngày giải ngân. Lãi tháng = lãi năm / 12.

Quy tắc chính:

- Lãi tính trên dư nợ đầu kỳ.
- Với `TRA_DEU`, tổng trả mỗi kỳ = `P x r / (1 - (1 + r)^-n)`, làm tròn đến đồng.
- Kỳ cuối của cả hai phương pháp trả toàn bộ dư nợ còn lại để dư nợ cuối bằng 0.
- Ngày đến hạn kỳ `k` = ngày giải ngân + `k` tháng, tính từ ngày gốc.
- Nếu ngày đến hạn rơi vào Thứ Bảy hoặc Chủ Nhật thì dời sang Thứ Hai kế tiếp.
- Từ chối số tiền <= 0, lãi suất âm hoặc trên 50%/năm, số kỳ ngoài 1-360.

Ví dụ PDF: vay **120.000.000 đ**, lãi **12%/năm**, **6 kỳ**, giải ngân **31/01/2026**. Tổng lãi `GOC_DEU` là **4.200.000 đ**, tổng lãi `TRA_DEU` là **4.234.824 đ**.

### Mindset

Bài này cần giữ rõ khái niệm dư nợ:

- `openingBalance`: dư nợ đầu kỳ.
- `interest`: lãi kỳ này.
- `principal`: gốc trả kỳ này.
- `payment`: tổng tiền trả.
- `closingBalance`: dư nợ sau khi trả.

Kỳ cuối là điểm hay bị lệch do làm tròn. Vì vậy kỳ cuối không nên dùng máy móc công thức cũ, mà lấy toàn bộ dư nợ còn lại làm phần gốc.

### Cách giải

`LoanScheduler.generate(LoanRequest, Method)` tạo danh sách `Installment`.

Luồng xử lý:

1. Validate `LoanRequest`.
2. Tính lãi suất tháng.
3. Với `GOC_DEU`, tính gốc đều cho các kỳ thường.
4. Với `TRA_DEU`, tính payment đều theo công thức annuity.
5. Lặp qua từng kỳ để tính lãi, gốc, payment, dư nợ cuối.
6. Kỳ cuối ép `closingBalance` về 0 bằng cách trả hết dư nợ còn lại.
7. Tính ngày đến hạn từ ngày giải ngân gốc, rồi dời cuối tuần nếu cần.

### Tổ chức code

- `Method`: enum `GOC_DEU`, `TRA_DEU`.
- `LoanRequest`: input khoản vay.
- `Installment`: một dòng lịch trả nợ.
- `LoanComparisonResult`: kết quả so sánh tổng lãi hai phương pháp.
- `LoanScheduler`: service sinh lịch và so sánh.
- `InvalidLoanRequestException`: lỗi input khoản vay.

---

## Bài 3 - Báo cáo doanh số từ file CSV (`bai3`)

### Đề bài yêu cầu gì

Phòng kinh doanh xuất dữ liệu đơn hàng ra CSV. File có header:

```text
orderId,orderDate,region,productCode,category,quantity,unitPrice,status
```

Quy tắc chính:

- `COMPLETED`: ghi tăng doanh thu.
- `RETURNED`: ghi giảm doanh thu.
- `CANCELLED`: bỏ qua doanh thu.
- Dòng lỗi bị loại nhưng chương trình không được dừng.
- Lỗi cần ghi số dòng và lý do.
- Tỷ lệ hoàn trả theo nhóm hàng = giá trị `RETURNED` / giá trị `COMPLETED`, hiển thị phần trăm 2 chữ số thập phân.

Yêu cầu kỹ thuật:

- Đọc file bằng `Files.lines`.
- Phần tổng hợp dùng Stream API, không dùng vòng lặp `for`/`while`.
- Trả về doanh thu thuần theo vùng giảm dần, top 3 sản phẩm, doanh thu theo tháng, tỷ lệ hoàn trả theo nhóm hàng và danh sách lỗi.

Ví dụ PDF có 3 dòng lỗi:

- Dòng 9: `O008`, số lượng không hợp lệ.
- Dòng 12: `O011`, ngày không hợp lệ.
- Dòng 13: `O002`, trùng mã đơn.

Kết quả đúng gồm:

- Theo vùng: HCM 20.200.000 đ; HN 9.950.000 đ; DN -2.000.000 đ.
- Tổng: 28.150.000 đ.
- Top 3 sản phẩm: P01 19.400.000 đ; P02 5.800.000 đ; P04 2.250.000 đ.
- Theo tháng: 2026-07 7.700.000 đ; 2026-08 16.650.000 đ; 2026-09 3.800.000 đ.
- Tỷ lệ hoàn trả: DIEN_TU 20,49%; GIA_DUNG 8,52%; THUC_PHAM 0,00%.

### Mindset

Bài này có 2 pha:

1. Parse và validate từng dòng CSV.
2. Tổng hợp các dòng hợp lệ.

Không nên trộn hai pha này vào nhau. Dòng lỗi cần được ghi nhận, còn dòng hợp lệ mới đi vào report. Như vậy code dễ kiểm soát và test dễ đọc.

### Cách giải

`SalesReporter.generateReport(Path)` xử lý như sau:

1. Đọc từng dòng bằng `Files.lines`.
2. Bỏ dòng header.
3. Kiểm tra số cột.
4. Parse ngày, số lượng, đơn giá, trạng thái.
5. Kiểm tra `orderId` trùng với các dòng trước.
6. Dòng hợp lệ tạo thành `OrderRecord`.
7. Dòng lỗi tạo thành `RowError`.
8. Dùng Stream API để group, sort, limit và tính tỷ lệ hoàn trả.

### Tổ chức code

- `OrderStatus`: enum trạng thái đơn hàng.
- `OrderRecord`: dữ liệu một dòng hợp lệ, có hàm tính giá trị dòng và tác động doanh thu thuần.
- `RowError`: dòng lỗi, mã đơn và lý do.
- `SalesReport`: kết quả báo cáo.
- `SalesReporter`: service đọc file và tổng hợp.

---

## Bài 4 - Sổ cái chuyển tiền nội bộ (`bai4`)

### Đề bài yêu cầu gì

Ví điện tử cần lõi chuyển tiền giữa các tài khoản khách hàng, ghi sổ theo nguyên tắc bút toán kép, chạy đúng khi nhiều yêu cầu đến đồng thời và khi client gửi lại cùng một yêu cầu.

Quy tắc chính:

- Mỗi giao dịch sinh `JournalEntry` gồm nhiều dòng Nợ/Có.
- Tổng Nợ phải bằng tổng Có.
- Số dư tài khoản chỉ thay đổi qua bút toán, không có setter.
- Phí chuyển = 0,1% số tiền, tối thiểu 2.000 đ, tối đa 50.000 đ, người gửi chịu.
- Bút toán chuyển tiền gồm 3 dòng:
  - Nợ tài khoản gửi: số tiền + phí.
  - Có tài khoản nhận: số tiền.
  - Có `FEE_INCOME`: phí.
- Số dư sau giao dịch không được thấp hơn `-overdraftLimit`.
- Tổng tiền chuyển đi trong ngày của một tài khoản không vượt 500.000.000 đ, không tính phí.
- Mỗi yêu cầu có `idempotencyKey`.
- Gửi lại cùng key, cùng nội dung thì trả kết quả cũ.
- Cùng key nhưng khác nội dung thì ném `IdempotencyConflictException`.
- Yêu cầu bị từ chối cũng được ghi nhớ.
- `reverse(journalId)` tạo bút toán đảo, chỉ đảo một lần.
- Sao kê gồm số dư đầu kỳ, từng dòng phát sinh, số dư cuối kỳ.
- `trialBalance` phải cho thấy tổng số dư mọi tài khoản, kể cả `FEE_INCOME`, bằng tổng số dư ban đầu.

Yêu cầu kỹ thuật trong PDF:

- `LedgerService` có `openAccount`, `transfer`, `reverse`, `statement`, `trialBalance`.
- Thời gian lấy từ `Clock` inject vào service để test hạn mức ngày.
- Thread-safe, không deadlock.
- Không dùng một khóa toàn cục cho mọi giao dịch.
- Hai giao dịch không chung tài khoản khách hàng phải chạy song song được.
- Test tải bắt buộc: 50 tài khoản, 8 luồng, mỗi luồng 10.000 giao dịch ngẫu nhiên, 10% gửi lại khóa cũ, kết thúc trong 30 giây.

### Mindset

Bài này phải nghĩ theo kế toán kép, không nghĩ đơn giản là "trừ A cộng B". Nếu chuyển 1.000.000 đ và phí 2.000 đ, bút toán đúng là:

- Debit A: 1.002.000 đ.
- Credit B: 1.000.000 đ.
- Credit `FEE_INCOME`: 2.000 đ.

Tổng debit = tổng credit = 1.002.000 đ. Vì vậy tổng số dư toàn hệ thống không tự nhiên tăng hoặc giảm.

Với đồng thời, trọng tâm là khóa đúng phạm vi. Nếu khóa toàn cục thì dễ đúng nhưng không đạt yêu cầu song song. Nếu khóa tùy tiện theo thứ tự yêu cầu thì dễ deadlock. Cách hợp lý là khóa các tài khoản theo thứ tự ID cố định.

### Cách giải

`LedgerService.transfer()` nên đi theo luồng:

1. Kiểm tra idempotency trước.
2. Validate tài khoản gửi, tài khoản nhận, số tiền.
3. Tính phí.
4. Xác định thứ tự khóa tài khoản theo `accountId`.
5. Trong vùng lock, kiểm tra thấu chi và hạn mức ngày.
6. Ghi posting vào tài khoản gửi, tài khoản nhận và `FEE_INCOME`.
7. Tạo `JournalEntry`.
8. Lưu kết quả vào idempotency store.

`reverse()` không sửa số dư thủ công. Nó tạo bút toán ngược:

- Bên từng được credit sẽ bị debit.
- Bên từng bị debit sẽ được credit.
- Phí cũng được đảo.

### Tổ chức code

- `Account`: tài khoản, số dư, hạn mức thấu chi, lịch sử posting và lock riêng.
- `EntryType`: `DEBIT`, `CREDIT`.
- `JournalLine`: một dòng bút toán.
- `JournalEntry`: một bút toán đầy đủ.
- `TransferRequest`: input chuyển tiền.
- `TransferResult`: output chuyển tiền.
- `StatementItem`, `AccountStatement`: dữ liệu sao kê.
- `TrialBalance`: bảng cân đối thử.
- `LedgerService`: service chính.
- Exception riêng: `InsufficientFundsException`, `DailyLimitExceededException`, `IdempotencyConflictException`, `AlreadyReversedException`, `AccountNotFoundException`, `InvalidTransactionException`.

Ghi chú đối chiếu PDF: đề yêu cầu test tải 8 x 10.000 giao dịch. Nếu muốn bám sát PDF, test hiện tại nên dùng đúng con số này; nếu giảm số giao dịch để chạy nhanh hơn thì nên ghi rõ trong README.

---

## Bài 5 - Động cơ khuyến mại tối ưu cho giỏ hàng (`bai5`)

### Đề bài yêu cầu gì

Trang thương mại điện tử chạy nhiều chương trình khuyến mại cùng lúc. Hệ thống phải tự chọn cách áp dụng có lợi nhất cho khách và giải thích được kết quả.

Các loại khuyến mại cần mô hình bằng `sealed interface Promotion`:

- `BuyXGetY`: cấp dòng, mua X tặng Y cùng sản phẩm.
- `CategoryPercent`: cấp dòng, giảm p% theo nhóm hàng, có trần tổng giảm tùy chọn.
- `Bundle`: cấp dòng, combo một tập sản phẩm bán với giá cố định.
- `OrderThreshold`: cấp đơn hàng, giá trị sau khuyến mại dòng đạt ngưỡng thì giảm số tiền cố định.
- `Voucher`: cấp đơn hàng, mã khách nhập, giảm p% sau mọi khuyến mại khác, có trần, tối đa 1 voucher/đơn.

Quy tắc chính:

- Khuyến mại hết hạn hoặc sai hạng khách bị loại trước khi tính.
- Mỗi đơn vị hàng chỉ thuộc tối đa một khuyến mại cấp dòng.
- Các đơn vị khác nhau của cùng một sản phẩm có thể thuộc các khuyến mại khác nhau.
- Khuyến mại cấp đơn áp theo thứ tự `OrderThreshold` rồi `Voucher`.
- Engine chọn phương án có số tiền khách trả nhỏ nhất.
- Nếu hòa, chọn phương án dùng ít khuyến mại dòng hơn.
- Nếu vẫn hòa, chọn phương án có tổng độ ưu tiên cao hơn.
- Mọi khoản giảm làm tròn đến đồng.
- Số tiền phải trả không âm.

Yêu cầu kỹ thuật:

- `PromotionEngine.evaluate(Cart, List<Promotion>, Customer, Optional<String> voucherCode, LocalDateTime at)` trả về `PricingResult`.
- `PricingResult` gồm tổng gốc, danh sách khuyến mại được áp, số tiền phải trả và đoạn giải thích dạng văn bản.
- Giỏ tối đa 30 dòng, 15 khuyến mại.
- Mỗi lần tính dưới 1 giây.
- Không duyệt vét cạn ngây thơ.
- README phải mô tả chiến lược tìm kiếm và độ phức tạp.
- Thêm loại khuyến mại mới không được phải sửa logic tìm kiếm cốt lõi.

Ví dụ PDF:

- Giỏ hàng tổng gốc: 170.000 đ.
- Sản phẩm:
  - `SP-A`, nước ngọt, `DO_UONG`, 10.000 đ, SL 6.
  - `SP-B`, bánh quy, `BANH_KEO`, 25.000 đ, SL 2.
  - `SP-C`, dầu ăn, `GIA_VI`, 60.000 đ, SL 1.
- Khuyến mại:
  - P1: `BuyXGetY` SP-A mua 2 tặng 1.
  - P2: `CategoryPercent` DO_UONG giảm 25%.
  - P3: `Bundle` 1 SP-A + 1 SP-B giá 30.000 đ.
  - P4: `OrderThreshold` từ 150.000 đ giảm 15.000 đ.
  - P5: `Voucher` SALE10 giảm 10%, tối đa 12.000 đ.

Kết quả đúng: áp P1 cho 6 SP-A, tức 2 bộ, giảm 20.000 đ; áp P4 giảm 15.000 đ; áp P5 giảm 12.000 đ. Khách trả **123.000 đ**.

Bẫy trong đề: phương án giảm cấp dòng lớn nhất có thể giảm 22.500 đ, nhưng làm giá trị sau khuyến mại dòng xuống 147.500 đ, mất P4, cuối cùng khách phải trả 135.500 đ. Vì vậy greedy theo mức giảm cấp dòng không đúng.

### Mindset

Bài này không phải bài "chọn khuyến mại giảm nhiều nhất". Nó là bài tối ưu có ràng buộc:

- Một đơn vị hàng không được dùng cho hai khuyến mại dòng.
- Giảm ở cấp dòng ảnh hưởng điều kiện khuyến mại cấp đơn.
- Kết quả cuối cùng mới là thứ cần tối ưu.

Do đó cần xét phương án tổng thể. Mỗi phương án gồm cách gán đơn vị hàng cho khuyến mại dòng, sau đó kéo theo khuyến mại cấp đơn.

### Cách giải

`PromotionEngine.evaluate()` nên xử lý theo các bước:

1. Lọc promotion theo thời gian hiệu lực và hạng khách.
2. Tách promotion theo loại.
3. Gom giỏ hàng thành inventory theo mã sản phẩm.
4. Từng promotion cấp dòng tự sinh `PromotionCandidate`.
5. Engine duyệt các candidate hợp lệ bằng Branch and Bound.
6. Với mỗi trạng thái, tính giá trị sau khuyến mại dòng.
7. Áp `OrderThreshold`, rồi `Voucher`.
8. So sánh với phương án tốt nhất hiện tại theo 3 tiêu chí trong đề.

### Vì sao dùng `PromotionCandidate`

`PromotionEngine` không nên biết chi tiết từng loại khuyến mại. `BuyXGetY`, `CategoryPercent`, `Bundle` có logic sinh phương án khác nhau. Nếu mỗi loại tự sinh `PromotionCandidate`, engine chỉ cần xử lý một dạng chung:

- candidate chiếm những đơn vị hàng nào,
- giảm bao nhiêu tiền,
- thuộc promotion nào,
- mô tả ra sao.

Nhờ vậy khi thêm promotion mới, phần tìm kiếm cốt lõi không cần sửa nhiều.

### Tổ chức code

- `Promotion`: sealed interface chung.
- `BuyXGetYPromotion`, `CategoryPercentPromotion`, `BundlePromotion`, `OrderThresholdPromotion`, `VoucherPromotion`: các loại khuyến mại.
- `PromotionCandidate`: phương án áp dụng một khuyến mại cấp dòng.
- `AppliedPromotion`: khuyến mại được chọn trong kết quả.
- `Cart`, `CartItem`: giỏ hàng.
- `Customer`, `CustomerTier`: khách hàng và hạng khách.
- `PricingResult`: kết quả cuối cùng.
- `PromotionEngine`: lọc promotion, tìm phương án, build kết quả giải thích.

---

## Cách trình bày khi bảo vệ bài

Có thể trình bày theo mẫu sau:

1. Bài này input là gì, output là gì.
2. Luật nghiệp vụ nào dễ sai nhất.
3. Em chia model dữ liệu ra sao.
4. Service chính xử lý theo các bước nào.
5. Test nào chứng minh case mẫu trong PDF.
6. Test nào chứng minh case biên và lỗi.

Với bài 1 và 2, nhấn mạnh `BigDecimal` và điểm làm tròn. Với bài 3, nhấn mạnh tách dòng lỗi khỏi dòng hợp lệ. Với bài 4, nhấn mạnh bút toán kép, idempotency và khóa theo thứ tự account ID. Với bài 5, nhấn mạnh lý do không dùng greedy và cách `PromotionCandidate` giúp mở rộng.
