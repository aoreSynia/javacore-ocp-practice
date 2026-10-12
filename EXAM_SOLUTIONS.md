# ĐÁP ÁN & GIẢI THÍCH CHI TIẾT ĐỀ THI JAVA CORE
## ĐỀ 1: LÝ THUYẾT (CHUẨN OCP JAVA SE 17 DEVELOPER - 1Z0-829)

Bộ đề gồm 50 câu trắc nghiệm phân bổ theo 11 nhóm mục tiêu trọng tâm của kỳ thi Oracle Certified Professional (OCP) Java SE 17.

---

### Nhóm 1 – Kiểu dữ liệu, String, ngày giờ, số (Câu 1 – 7)

#### Câu 1
**Đề bài:** Kết quả in ra là gì?
```java
var s = "Java";
s.concat(" 17");
s = s.toUpperCase();
System.out.println(s + s.length());
```
- **Đáp án:** **B. JAVA4**
- **Giải thích:**
  - `String` trong Java là đối tượng bất biến (`immutable`). Lệnh `s.concat(" 17")` tạo ra chuỗi mới `"Java 17"` nhưng giá trị này không được gán lại vào biến `s`, do đó `s` vẫn giữ nguyên là `"Java"`.
  - Tiếp theo, `s = s.toUpperCase()` gán chuỗi mới `"JAVA"` vào `s`.
  - Cuối cùng, `s.length()` trả về `4`. Kết quả nối chuỗi là `"JAVA" + 4 = "JAVA4"`.

---

#### Câu 2
**Đề bài:** Kết quả in ra là gì? (Mỗi dòng trong text block thụt 4 khoảng trắng, dòng “World” thụt 5)
```java
String t = """
    Hello\
     World
    """;
System.out.print(t.length());
```
- **Đáp án:** **A. 11**
- **Giải thích:**
  - Text Block (Java 15+) tự động tính toán khoảng thụt lề cơ sở (`incidental whitespace`) dựa trên vị trí của dấu đóng `"""`. Ở đây dấu đóng thụt 4 khoảng trắng.
  - Dòng 1: `Hello\` thụt 4 khoảng trắng $\rightarrow$ phần nội dung là `"Hello"`. Ký tự `\` ở cuối dòng là toán tử nối dòng (`line continuation`), loại bỏ ký tự xuống dòng `\n`.
  - Dòng 2: ` World` thụt 5 khoảng trắng $\rightarrow$ sau khi trừ 4 khoảng trắng cơ sở còn lại 1 khoảng trắng và `"World"` $\rightarrow$ `" World"`.
  - Chuỗi kết quả là `"Hello World"` gồm 5 + 1 + 5 = 11 ký tự.

---

#### Câu 3
**Đề bài:** Kết quả in ra là gì?
```java
Integer a = 127, b = 127, c = 128, d = 128;
System.out.println((a == b) + " " + (c == d) + " " + c.equals(d));
```
- **Đáp án:** **B. true false true**
- **Giải thích:**
  - Java có cơ chế `Integer Cache` lưu trữ các đối tượng Wrapper có giá trị trong đoạn từ `-128` đến `127`.
  - Với giá trị `127`, `a` và `b` cùng trỏ tới một đối tượng trong cache $\rightarrow$ `a == b` là `true`.
  - Với giá trị `128` (nằm ngoài cache), Java tạo ra hai đối tượng `Integer` riêng biệt trên Heap $\rightarrow$ `c == d` là `false`.
  - Phương thức `equals()` so sánh giá trị bên trong của hai đối tượng $\rightarrow$ `c.equals(d)` là `true`.

---

#### Câu 4
**Đề bài:** Phát biểu nào đúng?
```java
short x = 10;
x = x + 5; // dòng 1
x += 5;    // dòng 2
```
- **Đáp án:** **B. Chỉ dòng 1 lỗi biên dịch**
- **Giải thích:**
  - Tại dòng 1, biểu thức `x + 5` thực hiện phép toán số học giữa `short` và hằng số nguyên `int`, Java tự động thăng cấp kiểu kết quả lên `int`. Việc gán một giá trị `int` ngược lại cho biến `short` mà không có ép kiểu tường minh sẽ bị compiler từ chối.
  - Tại dòng 2, toán tử gán phức hợp (`compound assignment operator`) `x += 5` ngầm định ép kiểu tương đương `x = (short)(x + 5)` nên biên dịch thành công.

---

#### Câu 5
**Đề bài:** Kết quả in ra là gì?
```java
LocalDate d = LocalDate.of(2024, 1, 31);
d.plusMonths(1);
System.out.println(d.plusMonths(1).plusDays(1));
```
- **Đáp án:** **A. 2024-03-01**
- **Giải thích:**
  - `LocalDate` là immutable; dòng `d.plusMonths(1);` không làm thay đổi biến `d`.
  - 2024 là năm nhuận. Khi cộng 1 tháng vào ngày 31/01/2024, tháng 2 chỉ có 29 ngày, Java tự động kẹp về ngày cuối cùng hợp lệ của tháng: `2024-02-29`.
  - Tiếp tục gọi `.plusDays(1)` trên ngày `2024-02-29` sẽ chuyển sang ngày `2024-03-01`.

---

#### Câu 6
**Đề bài:** Kết quả in ra là gì?
```java
var p = Period.ofYears(1).ofMonths(2);
System.out.println(p);
```
- **Đáp án:** **B. P2M**
- **Giải thích:**
  - Các phương thức khởi tạo của `Period` (`ofYears`, `ofMonths`, `ofDays`) là các **static method**.
  - Việc gọi nối chuỗi `Period.ofYears(1).ofMonths(2)` không tích lũy các trường mà thực chất là gọi phương thức static `Period.ofMonths(2)`, giá trị năm bị bỏ qua hoàn toàn. Kết quả in theo chuẩn ISO-8601 là `P2M`.

---

#### Câu 7
**Đề bài:** Kết quả in ra là gì?
```java
var a = new BigDecimal("1.10");
var b = new BigDecimal("1.1");
System.out.println(a.equals(b) + " " + (a.compareTo(b) == 0) + " " + a.add(b));
```
- **Đáp án:** **B. false true 2.20**
- **Giải thích:**
  - `BigDecimal.equals()` so sánh cả giá trị số học lẫn độ chính xác (`scale`). `a` có scale là 2, `b` có scale là 1 $\rightarrow$ `equals()` trả về `false`.
  - `BigDecimal.compareTo()` chỉ so sánh giá trị số học $\rightarrow$ trả về `0` (bằng nhau) $\rightarrow$ `true`.
  - Phép cộng `a.add(b)` lấy scale lớn nhất của hai toán hạng (scale 2) $\rightarrow$ kết quả là `2.20`.

---

### Nhóm 2 – Điều khiển luồng (Câu 8 – 11)

#### Câu 8
**Đề bài:** Kết quả in ra là gì?
```java
int day = 6;
String type = switch (day) {
    case 1, 2, 3, 4, 5 -> "WEEKDAY";
    case 6, 7 -> {
        String t = "WEEKEND";
        yield t.toLowerCase();
    }
    default -> "INVALID";
};
System.out.println(type);
```
- **Đáp án:** **B. weekend**
- **Giải thích:** Trong switch expression với cú pháp mũi tên `->`, khi có khối lệnh `{}` thì phải dùng từ khóa `yield` để trả về giá trị cho biểu thức. Biến `day = 6` rơi vào nhánh `case 6, 7`, chuỗi `"WEEKEND"` được chuyển thành `"weekend"`.

---

#### Câu 9
**Đề bài:** Kết quả in ra là gì?
```java
int count = 0;
outer:
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (j == 1) continue outer;
        if (i == 2) break outer;
        count++;
    }
}
System.out.println(count);
```
- **Đáp án:** **C. 3**
- **Giải thích:**
  - `i = 0`: `j = 0` $\rightarrow$ count = 1. Khi `j = 1` $\rightarrow$ `continue outer` (nhảy sang `i = 1`).
  - `i = 1`: `j = 0` $\rightarrow$ count = 2. Khi `j = 1` $\rightarrow$ `continue outer` (nhảy sang `i = 2`).
  - `i = 2`: `j = 0` $\rightarrow$ count = 3. Khi `j = 1` $\rightarrow$ `continue outer` (kết thúc vòng ngoài vì `i < 3`).
  - Điều kiện `if (i == 2) break outer` không bao giờ được chạm tới vì khi `j = 0` điều kiện đó sai, còn khi `j = 1` thì đã bị `continue outer` từ dòng trước. Tổng `count = 3`.

---

#### Câu 10
**Đề bài:** Kết quả in ra là gì?
```java
Object o = 42;
if (o instanceof Integer i && i > 40) {
    System.out.print("A");
}
if (!(o instanceof String s)) {
    System.out.print("B");
} else {
    System.out.print(s.length());
}
```
- **Đáp án:** **B. AB**
- **Giải thích:**
  - Nhánh 1: `o` là `Integer` có giá trị `42 > 40` $\rightarrow$ in `"A"`.
  - Nhánh 2: Cơ chế Flow Scoping trong Pattern Matching: Vì có dấu phủ định `!`, biến mẫu `s` chỉ tồn tại trong nhánh `else`. Biểu thức `!(o instanceof String)` là `true` $\rightarrow$ khối `if` thực thi và in `"B"`. Kết quả là `"AB"`.

---

#### Câu 11
**Đề bài:** Kết quả in ra là gì?
```java
int x = 2;
switch (x) {
    case 1: System.out.print("1");
    case 2: System.out.print("2");
    case 3: System.out.print("3"); break;
    default: System.out.print("D");
}
```
- **Đáp án:** **B. 23**
- **Giải thích:** Đây là cú pháp switch statement truyền thống dùng dấu hai chấm `:`. Khối `case 2` in `"2"` nhưng không có lệnh `break`, dẫn đến hiện tượng trôi nhánh (`fall-through`) sang `case 3`, in `"3"` rồi gặp `break` thoát khỏi switch.

---

### Nhóm 3 – Lập trình hướng đối tượng (Câu 12 – 21)

#### Câu 12
**Đề bài:** Kết quả in ra là gì?
```java
public record Money(String currency, long amount) {
    public Money {
        if (amount < 0) throw new IllegalArgumentException();
        currency = currency.toUpperCase();
    }
}
// trong main:
var m = new Money("vnd", 1000);
System.out.println(m.currency() + m);
```
- **Đáp án:** **B. VNDMoney[currency=VND, amount=1000]**
- **Giải thích:** Compact constructor của `record` cho phép chuẩn hóa hoặc gán lại các tham số trước khi compiler tự động gán chúng vào các instance field ngầm định ở cuối constructor. `m.currency()` là `"VND"`, và phương thức `toString()` mặc định của record sinh chuỗi `Money[currency=VND, amount=1000]`.

---

#### Câu 13
**Đề bài:** Phát biểu nào đúng về `record`? (Chọn 2)
- **Đáp án:** **B và C**
  - **B. Record có thể implement interface** (Đúng).
  - **C. Các thành phần của record ngầm định là private final** (Đúng).
- **Giải thích:** Record ngầm định kế thừa `java.lang.Record` và là `final class`, không thể kế thừa class khác (loại A), không thể khai báo thêm instance field ngoài constructor component (loại D), và không thể là abstract (loại E).

---

#### Câu 14
**Đề bài:** Dòng nào gây lỗi biên dịch?
```java
sealed interface Payment permits Cash, Card {} // dòng 1
final class Cash implements Payment {}          // dòng 2
non-sealed class Card implements Payment {}     // dòng 3
class CreditCard extends Card {}                // dòng 4
class Voucher implements Payment {}             // dòng 5
```
- **Đáp án:** **C. Dòng 5**
- **Giải thích:** `Payment` là `sealed interface` và chỉ cấp quyền kế thừa (`permits`) cho `Cash` và `Card`. Bất kỳ lớp nào khác cố gắng implement `Payment` (ở đây là `Voucher`) mà không được liệt kê trong mệnh đề `permits` sẽ gây lỗi biên dịch.

---

#### Câu 15
**Đề bài:** Kết quả in ra là gì?
```java
class Account { String type = "BASE"; String getType() { return type; } }
class Saving extends Account { String type = "SAVING"; String getType() { return type; } }
// trong main:
Account a = new Saving();
System.out.println(a.type + " " + a.getType());
```
- **Đáp án:** **C. BASE SAVING**
- **Giải thích:**
  - Truy cập biến `a.type` được phân giải tại thời điểm biên dịch dựa trên kiểu tham chiếu của biến (`Account`), field trong Java không có tính đa hình $\rightarrow$ in `"BASE"`.
  - Lời gọi phương thức `a.getType()` là đa hình (Virtual Method Invocation), thực thi phương thức bị ghi đè của đối tượng thực tế tại runtime (`Saving`) $\rightarrow$ in `"SAVING"`.

---

#### Câu 16
**Đề bài:** Kết quả in ra là gì?
```java
class A {
    static { System.out.print("1"); }
    { System.out.print("2"); }
    A() { System.out.print("3"); }
}
class B extends A {
    static { System.out.print("4"); }
    { System.out.print("5"); }
    B() { System.out.print("6"); }
}
// trong main:
new B(); new B();
```
- **Đáp án:** **A. 1423562356**
- **Giải thích:**
  - Khối `static` chỉ chạy một lần duy nhất khi nạp lớp: cha trước (`1`), con sau (`4`).
  - Lần tạo đối tượng thứ nhất: Instance init cha (`2`) $\rightarrow$ Constructor cha (`3`) $\rightarrow$ Instance init con (`5`) $\rightarrow$ Constructor con (`6`) $\rightarrow$ sinh chuỗi `142356`.
  - Lần tạo đối tượng thứ hai: Các khối static không chạy lại, chỉ lặp lại thứ tự tạo đối tượng $\rightarrow$ `2356`. Ghép lại thành `1423562356`.

---

#### Câu 17
**Đề bài:** Kết quả in ra là gì?
```java
interface Discount {
    double rate();
    default double apply(double price) { return price * (1 - rate()); }
    private void log() { }
    static Discount of(double r) { return () -> r; }
}
// trong main:
System.out.println(Discount.of(0.25).apply(200));
```
- **Đáp án:** **A. 150.0**
- **Giải thích:** Java 9+ cho phép phương thức `private` trong interface. `Discount` chỉ có đúng một phương thức trừu tượng (`rate()`), nên hoàn toàn là một Functional Interface hợp lệ. `Discount.of(0.25)` trả về lambda cài đặt `rate() = 0.25`. Kết quả áp dụng là `200 * (1 - 0.25) = 150.0`.

---

#### Câu 18
**Đề bài:** Kết quả in ra là gì?
```java
static void pay(long x) { System.out.print("long "); }
static void pay(Integer x) { System.out.print("Integer "); }
static void pay(int... x) { System.out.print("varargs "); }
// trong main:
pay(5);
```
- **Đáp án:** **A. long**
- **Giải thích:** Thứ tự ưu tiên giải quyết nạp chồng phương thức (Method Overloading) trong Java:
  1. Mở rộng kiểu nguyên thủy (`Widening`: `int` $\rightarrow$ `long`).
  2. Đóng hộp (`Autoboxing`: `int` $\rightarrow$ `Integer`).
  3. Tham số biến đổi (`Varargs`: `int...`).
  Do đó Java ưu tiên chọn phương thức nhận `long`.

---

#### Câu 19
**Đề bài:** Kết quả in ra là gì?
```java
abstract class Product {
    abstract double price();
    Product() { System.out.print(price()); }
}
class Book extends Product {
    double p = 100;
    double price() { return p; }
}
// trong main:
new Book();
```
- **Đáp án:** **B. 0.0**
- **Giải thích:** Khi constructor của lớp cha `Product()` được gọi, các trường của lớp con (`Book.p = 100`) chưa được khởi tạo và vẫn mang giá trị mặc định của kiểu nguyên thủy `double` là `0.0`. Do phương thức `price()` bị ghi đè, nó được gọi và trả về giá trị ban đầu là `0.0`.

---

#### Câu 20
**Đề bài:** Kết quả in ra là gì?
```java
enum Tier {
    SILVER(0.02), GOLD(0.05), PLATINUM(0.10);
    private final double rate;
    Tier(double r) { rate = r; }
    double rate() { return rate; }
}
// trong main:
System.out.println(Tier.valueOf("GOLD").ordinal() + Tier.GOLD.rate());
```
- **Đáp án:** **A. 1.05**
- **Giải thích:** Thứ tự `ordinal()` của `SILVER` là 0, của `GOLD` là 1. `Tier.GOLD.rate()` là `0.05`. Biểu thức tính `1 + 0.05 = 1.05`.

---

#### Câu 21
**Đề bài:** Giả định `hashCode()` mặc định của hai đối tượng khác nhau. Kết quả in ra là gì?
```java
class Sku {
    final String code;
    Sku(String c) { code = c; }
    @Override public boolean equals(Object o) {
        return o instanceof Sku s && s.code.equals(code);
    }
}
// trong main:
Set<Sku> set = new HashSet<>();
set.add(new Sku("A"));
set.add(new Sku("A"));
System.out.println(set.size());
```
- **Đáp án:** **B. 2**
- **Giải thích:** `Sku` ghi đè phương thức `equals()` nhưng **không ghi đè `hashCode()`**. Khi hai đối tượng có mã hash khác nhau, `HashSet` phân bổ chúng vào hai bucket khác nhau mà không bao giờ gọi đến `equals()`. Do đó cả hai đối tượng đều được thêm vào Set.

---

### Nhóm 4 – Xử lý ngoại lệ (Câu 22 – 26)

#### Câu 22
**Đề bài:** Kết quả in ra là gì?
```java
class Res implements AutoCloseable {
    final String n;
    Res(String n) { this.n = n; }
    public void close() { System.out.print("C" + n); }
}
// trong main:
try (var a = new Res("1"); var b = new Res("2")) {
    System.out.print("T");
    throw new RuntimeException();
} catch (Exception e) {
    System.out.print("E");
} finally {
    System.out.print("F");
}
```
- **Đáp án:** **C. TC2C1EF**
- **Giải thích:**
  - Thân `try` chạy và in `"T"`, sau đó ngoại lệ xảy ra.
  - Các tài nguyên trong `try-with-resources` được tự động đóng theo thứ tự **ngược lại** với thứ tự khai báo: đóng `b` (in `"C2"`), sau đó đóng `a` (in `"C1"`).
  - Tiếp theo khối `catch` bắt ngoại lệ và in `"E"`.
  - Cuối cùng khối `finally` chạy và in `"F"`. Kết quả là `TC2C1EF`.

---

#### Câu 23
**Đề bài:** Kiểu nào điền vào chỗ trống để code biên dịch được? (Chọn 2)
```java
class Repo { void save() throws IOException { } }
class SqlRepo extends Repo {
    @Override void save() throws ______ { }
}
```
- **Đáp án:** **B và D**
  - **B. FileNotFoundException** (là checked exception lớp con của `IOException`).
  - **D. IllegalStateException** (là unchecked exception, được phép ném bất kỳ lúc nào).
- **Giải thích:** Phương thức ghi đè không được phép khai báo ném các Checked Exception mới hoặc rộng hơn phương thức cha (`Exception`, `Throwable`, `SQLException` đều bị lỗi).

---

#### Câu 24
**Đề bài:** `calc()` trả về gì?
```java
static int calc() {
    int x = 1;
    try { return x; }
    finally { x = 2; }
}
```
- **Đáp án:** **A. 1**
- **Giải thích:** Tại dòng `return x;`, giá trị của `x` (`1`) được sao chép vào ô nhớ trả về trước khi quyền điều khiển được chuyển cho khối `finally`. Mặc dù khối `finally` thay đổi `x = 2`, nó không có câu lệnh `return` để ghi đè giá trị trả về trước đó, do đó hàm vẫn trả về `1`.

---

#### Câu 25
**Đề bài:** Biết `repo.call()` khai báo `throws IOException, SQLException`. Khối nào biên dịch được khi đặt sau `try { ... }` phù hợp?
- **Đáp án:** **D. `catch (IllegalArgumentException | IllegalStateException e) { }` với try chỉ gọi `Integer.parseInt("1")`**
- **Giải thích:**
  - Đáp án A lỗi vì `FileNotFoundException` là con của `IOException`, không được dùng trong multi-catch (`|`).
  - Đáp án B lỗi vì biến `e` trong multi-catch ngầm định là `final`, không được gán lại `e = ...`.
  - Đáp án C lỗi vì bắt `IOException` trước `FileNotFoundException` (unreachable catch block).
  - Đáp án D hợp lệ hoàn toàn với các Unchecked Exception.

---

#### Câu 26
**Đề bài:** Kết quả in ra là gì?
```java
class R implements AutoCloseable {
    public void close() throws Exception { throw new Exception("close"); }
}
// trong main:
try (R r = new R()) {
    throw new IllegalStateException("body");
} catch (Exception e) {
    System.out.println(e.getMessage() + " " + e.getSuppressed().length);
}
```
- **Đáp án:** **C. body 1**
- **Giải thích:** Ngoại lệ ném ra trong thân `try` là ngoại lệ chính (`primary exception`), thông điệp của nó là `"body"`. Ngoại lệ xảy ra trong phương thức `close()` tự động được đính kèm vào ngoại lệ chính dưới dạng ngoại lệ bị nén (`suppressed exception`). Do đó `e.getMessage()` là `"body"` và mảng `getSuppressed()` có độ dài là `1`.

---

### Nhóm 5 – Mảng, Collections, Generics (Câu 27 – 31)

#### Câu 27
**Đề bài:** Kết quả khi chạy là gì?
```java
List<String> a = List.of("A", "B");
List<String> b = new ArrayList<>(a);
b.add("C");
List<String> c = Arrays.asList("X", "Y");
c.set(0, "Z");
System.out.println(b + " " + c);
a.add("D");
```
- **Đáp án:** **B. In [A, B, C] [Z, Y] rồi ném UnsupportedOperationException**
- **Giải thích:** `b` là `ArrayList` thông thường nên `add("C")` thành công. `c` tạo bởi `Arrays.asList()` có kích thước cố định nhưng cho phép thay đổi phần tử qua `.set(0, "Z")`. Lệnh in hiển thị `[A, B, C] [Z, Y]`. Tuy nhiên `a` tạo bởi `List.of()` là danh sách hoàn toàn bất biến, lời gọi `a.add("D")` ném `UnsupportedOperationException`.

---

#### Câu 28
**Đề bài:** Bảng hạng thành viên theo doanh số tích lũy. Kết quả in ra là gì?
```java
TreeMap<Integer, String> tiers = new TreeMap<>(
    Map.of(0, "BRONZE", 5_000_000, "SILVER", 20_000_000, "GOLD"));
System.out.println(tiers.floorEntry(12_000_000).getValue()
    + " " + tiers.ceilingKey(20_000_001));
```
- **Đáp án:** **B. SILVER null**
- **Giải thích:**
  - `floorEntry(12_000_000)` tìm entry có key lớn nhất thỏa mãn $\le 12.000.000$, đó là `5_000_000 -> "SILVER"`.
  - `ceilingKey(20_000_001)` tìm key nhỏ nhất thỏa mãn $\ge 20.000.001$, trong map không có key nào lớn hơn nên trả về `null`.

---

#### Câu 29
**Đề bài:** Kết quả in ra là gì?
```java
int[] a = {1, 3, 5, 7};
System.out.println(Arrays.binarySearch(a, 4));
```
- **Đáp án:** **C. -3**
- **Giải thích:** Giá trị `4` không có trong mảng. Vị trí chèn (`insertion point`) để mảng vẫn giữ thứ tự tăng dần là vị trí index 2 (giữa `3` và `5`). Theo quy ước của `Arrays.binarySearch`, giá trị trả về là `-(insertion point + 1) = -(2 + 1) = -3`.

---

#### Câu 30
**Đề bài:** Kết quả in ra là gì?
```java
Deque<String> d = new ArrayDeque<>();
d.push("A");
d.offer("B");
d.addFirst("C");
System.out.println(d.pop() + d.pollLast() + d.peek());
```
- **Đáp án:** **A. CBA**
- **Giải thích:**
  - `push("A")` đưa vào đầu: `[A]`
  - `offer("B")` đưa vào cuối: `[A, B]`
  - `addFirst("C")` đưa vào đầu: `[C, A, B]`
  - `d.pop()` lấy và xóa phần tử đầu: `"C"`, deque còn `[A, B]`.
  - `d.pollLast()` lấy và xóa phần tử cuối: `"B"`, deque còn `[A]`.
  - `d.peek()` xem phần tử đầu: `"A"`. Ghép lại thành `"CBA"`.

---

#### Câu 31
**Đề bài:** Những dòng nào KHÔNG biên dịch? (Chọn 2)
```java
List<? extends Number> nums = new ArrayList<Integer>();
nums.add(1);                                            // L1
Number n = nums.isEmpty() ? null : nums.get(0);         // L2
List<? super Integer> ints = new ArrayList<Number>();
ints.add(1);                                            // L3
Integer i = ints.get(0);                                // L4
```
- **Đáp án:** **A và D (L1 và L4)**
- **Giải thích:**
  - L1: Wildcard covariance `? extends Number` chỉ cho phép đọc dữ liệu (Producer), không thể ghi bất kỳ giá trị nào vào danh sách vì compiler không thể xác định kiểu cụ thể $\rightarrow$ Lỗi biên dịch.
  - L4: Wildcard contravariance `? super Integer` chỉ đảm bảo phần tử đọc ra là kiểu `Object`, không thể gán trực tiếp cho `Integer` mà không ép kiểu $\rightarrow$ Lỗi biên dịch.

---

### Nhóm 6 – Stream API và Lambda (Câu 32 – 39)

#### Câu 32
**Đề bài:** Kết quả in ra là gì?
```java
var orders = List.of(120, 80, 200, 50);
int total = orders.stream()
    .filter(x -> x >= 80)
    .mapToInt(Integer::intValue)
    .sum();
System.out.println(total);
```
- **Đáp án:** **B. 400**
- **Giải thích:** Các số thỏa mãn $\ge 80$ là 120, 80, 200. Tổng của chúng là `120 + 80 + 200 = 400`.

---

#### Câu 33
**Đề bài:** Kết quả in ra là gì?
```java
Stream.of("a", "b", "c")
    .peek(System.out::print)
    .filter(s -> s.equals("b"))
    .findFirst();
```
- **Đáp án:** **B. ab**
- **Giải thích:** Stream trong Java hoạt động theo cơ chế đánh giá lười (`lazy evaluation`) và ngắn mạch (`short-circuiting`).
  - Phần tử `"a"` đi qua `peek` (in `"a"`), đến `filter` trả về `false`.
  - Phần tử `"b"` đi qua `peek` (in `"b"`), đến `filter` trả về `true` $\rightarrow$ thỏa mãn `findFirst()` $\rightarrow$ Stream dừng xử lý ngay lập tức. Phần tử `"c"` không bao giờ được duyệt tới.

---

#### Câu 34
**Đề bài:** Kết quả in ra là gì?
```java
record Sale(String region, int amount) { }
var sales = List.of(new Sale("HN", 100), new Sale("HCM", 200), new Sale("HN", 50));
Map<String, Integer> m = sales.stream().collect(
    Collectors.groupingBy(Sale::region, TreeMap::new,
        Collectors.summingInt(Sale::amount)));
System.out.println(m);
```
- **Đáp án:** **B. {HCM=200, HN=150}**
- **Giải thích:** `TreeMap` sắp xếp các key theo thứ tự tự nhiên (bảng chữ cái), do đó `"HCM"` đứng trước `"HN"`. Tổng doanh số của `"HCM"` là 200, của `"HN"` là 100 + 50 = 150.

---

#### Câu 35
**Đề bài:** Kết quả khi chạy là gì?
```java
var m = Stream.of("apple", "avocado", "banana")
    .collect(Collectors.toMap(s -> s.charAt(0), String::length));
System.out.println(m);
```
- **Đáp án:** **D. Ném IllegalStateException**
- **Giải thích:** Cả hai từ `"apple"` và `"avocado"` đều bắt đầu bằng chữ cái `'a'`, dẫn đến xung đột trùng key trong Map. Phương thức `Collectors.toMap` 2 tham số mặc định không có merge function sẽ ném ngoại lệ `java.lang.IllegalStateException: Duplicate key a`.

---

#### Câu 36
**Đề bài:** Dòng nào KHÔNG biên dịch?
```java
A. BiFunction<Integer, Integer, Integer> f = Integer::sum;
B. ToIntFunction<String> g = String::length;
C. Consumer<String> c = String::length;
D. Supplier<String> s = String::new;
E. UnaryOperator<Integer> u = (int x) -> x + 1;
```
- **Đáp án:** **C. Consumer<String> c = String::length;**
- **Giải thích:** `Consumer<T>` có phương thức hàm `void accept(T t)`, đòi hỏi trả về kiểu `void`. Trong khi đó `String::length` trả về kiểu `int`, do đó không thể gán vào `Consumer`.

---

#### Câu 37
**Đề bài:** Kết quả in ra là gì?
```java
Optional<String> o = Optional.empty();
System.out.println(o.map(String::length).orElseGet(() -> -1)
    + " " + Optional.ofNullable(null).isPresent());
```
- **Đáp án:** **A. -1 false**
- **Giải thích:** `o` là rỗng, `.map()` tiếp tục trả về empty Optional, sau đó `.orElseGet(() -> -1)` trả về giá trị mặc định `-1`. Biểu thức `Optional.ofNullable(null)` tạo empty Optional nên `.isPresent()` trả về `false`.

---

#### Câu 38
**Đề bài:** Kết quả in ra là gì?
```java
var prices = List.of(new BigDecimal("10.5"), new BigDecimal("4.50"));
System.out.println(prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
```
- **Đáp án:** **C. 15.00**
- **Giải thích:** `BigDecimal.ZERO` cộng với `10.5` (scale 1) ra `10.5`. Sau đó cộng tiếp với `4.50` (scale 2). Khi cộng hai `BigDecimal`, kết quả sẽ mang scale lớn nhất trong hai toán hạng $\rightarrow$ scale là 2, giá trị là `15.00`.

---

#### Câu 39
**Đề bài:** Kết quả in ra là gì?
```java
var names = List.of("Dung", "An", "Linh", "Bao");
names.stream()
    .sorted(Comparator.comparing(String::length)
        .thenComparing(Comparator.reverseOrder()))
    .forEach(System.out::print);
```
- **Đáp án:** **B. AnBaoLinhDung** *(hoặc phân tích thứ tự đảo ngược)*
- **Giải thích:**
  - Tiêu chí 1: Độ dài tăng dần. Chuỗi `"An"` có độ dài 2 ngắn nhất, đứng đầu tiên.
  - Ba chuỗi còn lại `"Dung"`, `"Linh"`, `"Bao"` đều có độ dài 4.
  - Tiêu chí 2: `Comparator.reverseOrder()` sắp xếp thứ tự nghịch đảo của chuỗi.

---

### Nhóm 7 – Đóng gói, Module (Câu 40 – 41)

#### Câu 40
**Đề bài:** Cho khai báo module sau. Phát biểu nào đúng?
```java
module com.shop.order {
    requires com.shop.payment;
    exports com.shop.order.api;
}
```
- **Đáp án:** **A. Module khác chỉ truy cập được kiểu public trong com.shop.order.api**
- **Giải thích:** Hệ thống module của Java (JPMS) áp dụng nguyên tắc đóng gói mạnh mẽ: Một module chỉ cho phép các module khác truy cập vào các kiểu dữ liệu `public` nằm trong các package được công khai tường minh bằng từ khóa `exports`.

---

#### Câu 41
**Đề bài:** Lệnh nào in ra khai báo module (requires, exports) có trong file `order.jar`?
- **Đáp án:** **B. jar --file order.jar --describe-module**
- **Giải thích:** Lệnh chuẩn của công cụ `jar` trong JDK để kiểm tra mô tả module (`module-info.class`) là `--describe-module` hoặc viết tắt là `-d`.

---

### Nhóm 8 – Đồng thời (Concurrency) (Câu 42 – 45)

#### Câu 42
**Đề bài:** Kết quả in ra là gì?
```java
var counter = new AtomicInteger();
var ex = Executors.newFixedThreadPool(4);
for (int i = 0; i < 1000; i++) ex.submit(counter::incrementAndGet);
ex.shutdown();
ex.awaitTermination(1, TimeUnit.MINUTES);
System.out.println(counter.get());
```
- **Đáp án:** **A. Luôn là 1000**
- **Giải thích:** `AtomicInteger` sử dụng các chỉ thị phần cứng nguyên tử (Compare-And-Swap / CAS), đảm bảo tính toàn vẹn khi tăng giá trị từ nhiều luồng. Lệnh `awaitTermination` đảm bảo toàn bộ 1000 tác vụ đã hoàn thành trước khi lấy giá trị.

---

#### Câu 43
**Đề bài:** Nhiều luồng cùng cộng doanh thu theo mã cửa hàng vào một map. Cách nào luôn cho tổng đúng?
- **Đáp án:** **C. ConcurrentHashMap với map.merge(k, v, Integer::sum)**
- **Giải thích:** Phương thức `ConcurrentHashMap.merge()` thực hiện việc đọc, tính toán và ghi đè một cách nguyên tử (`atomic`) trên bucket của key đó, đảm bảo không bị mất cập nhật khi nhiều luồng cùng thao tác. Ngược lại, việc gọi `map.put(k, map.get(k) + v)` là thao tác ghép rời rạc, vẫn gây ra race condition.

---

#### Câu 44
**Đề bài:** Hàm `transfer(from, to, amt)` khóa `synchronized(from)` rồi `synchronized(to)`. Hai luồng chạy đồng thời $A \rightarrow B$ và $B \rightarrow A$. Cách nào loại bỏ deadlock mà vẫn đảm bảo tính đúng?
- **Đáp án:** **B. Luôn khóa hai tài khoản theo thứ tự cố định, ví dụ id tăng dần**
- **Giải thích:** Deadlock xảy ra khi xuất hiện chu trình chờ đợi tài nguyên (Circular Wait). Bằng cách luôn khóa tài khoản theo thứ tự sắp xếp cố định (ví dụ so sánh Account ID), luồng nào cũng sẽ phải xin khóa tài khoản có ID nhỏ hơn trước, loại bỏ hoàn toàn khả năng xảy ra chu trình khóa chéo.

---

#### Câu 45
**Đề bài:** Kết quả khi chạy là gì?
```java
List<Integer> out = new ArrayList<>();
IntStream.range(0, 1000).parallel().forEach(out::add);
System.out.println(out.size());
```
- **Đáp án:** **C. Không xác định: có thể in số nhỏ hơn 1000 hoặc ném ngoại lệ**
- **Giải thích:** `ArrayList` không thread-safe. Khi nhiều luồng trong parallel stream cùng gọi `add()`, hiện tượng xung đột chỉ số mảng nội bộ xảy ra, dẫn đến ghi đè mất phần tử (kích thước $< 1000$) hoặc ném ngoại lệ `ArrayIndexOutOfBoundsException`.

---

### Nhóm 9 – I/O và NIO.2 (Câu 46 – 47)

#### Câu 46
**Đề bài:** Kết quả in ra là gì (hệ thống Linux)?
```java
Path p = Path.of("/data/reports/../invoices/./2026.csv");
Path n = p.normalize();
System.out.println(n.getFileName() + " " + n.getNameCount());
```
- **Đáp án:** **A. 2026.csv 3**
- **Giải thích:**
  - `p.normalize()` chuẩn hóa đường dẫn: `reports/..` triệt tiêu lẫn nhau, `.` bị loại bỏ $\rightarrow$ đường dẫn trở thành `/data/invoices/2026.csv`.
  - `n.getFileName()` là `"2026.csv"`.
  - `n.getNameCount()` đếm số phần tử trong đường dẫn (không tính root `/`): gồm `data`, `invoices`, `2026.csv` $\rightarrow$ 3.

---

#### Câu 47
**Đề bài:** Phát biểu nào đúng?
- **Đáp án:** **B. Files.lines trả về Stream<String> đọc lười và cần đóng bằng try-with-resources**
- **Giải thích:** `Files.lines()` mở một kênh đọc trực tiếp từ file disk và trả về Stream lười. Nó giữ tài nguyên file descriptor cho đến khi stream được đóng, vì vậy bắt buộc phải bao bọc trong `try-with-resources` để tránh rò rỉ tài nguyên hệ thống.

---

### Nhóm 10 – JDBC (Câu 48 – 49)

#### Câu 48
**Đề bài:** Phát biểu nào đúng về đoạn code trừ tiền sau?
```java
String sql = "UPDATE account SET balance = balance - ? WHERE id = ? AND balance >= ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setBigDecimal(1, amount);
    ps.setLong(2, accountId);
    ps.setBigDecimal(3, amount);
    int rows = ps.executeUpdate();
}
```
- **Đáp án:** **B. rows == 0 nghĩa là không có dòng nào thỏa điều kiện (sai id hoặc không đủ số dư)**
- **Giải thích:** Chỉ số tham số trong JDBC bắt đầu từ 1. `executeUpdate()` trả về số lượng dòng bị ảnh hưởng trong database. Nếu `rows == 0`, câu lệnh SQL không khớp dòng nào trong mệnh đề `WHERE` (có thể do ID không tồn tại hoặc số dư hiện tại không đủ để trừ).

---

#### Câu 49
**Đề bài:** Nếu `credit(...)` ném `SQLException`, điều gì xảy ra với thao tác `debit(...)`?
```java
conn.setAutoCommit(false);
try {
    debit(conn, from, amount);
    credit(conn, to, amount);
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
}
```
- **Đáp án:** **B. Bị hoàn tác bởi rollback()**
- **Giải thích:** Với `autoCommit = false`, tất cả các câu lệnh nằm trong cùng một transaction. Khi `credit` ném ngoại lệ, luồng điều khiển nhảy vào khối `catch` và thực thi `conn.rollback()`, toàn bộ các thay đổi trước đó của `debit` sẽ được hoàn tác sạch sẽ trong database.

---

### Nhóm 11 – Bản địa hóa (Localization) (Câu 50)

#### Câu 50
**Đề bài:** Classpath có `Messages.properties`, `Messages_en.properties`, `Messages_vi.properties`. Locale mặc định là `en_US`. Khóa `greeting` chỉ có trong `Messages_en.properties` và `Messages.properties`. Giá trị được lấy từ đâu?
```java
var rb = ResourceBundle.getBundle("Messages", Locale.forLanguageTag("vi-VN"));
System.out.println(rb.getString("greeting"));
```
- **Đáp án:** **B. Messages_en.properties**
- **Giải thích:** Thứ tự tìm kiếm ResourceBundle của Java:
  1. `Messages_vi_VN` (không có).
  2. `Messages_vi` (có file nhưng không chứa key `greeting`).
  3. Tìm tiếp theo Locale mặc định của hệ thống (`en_US`): `Messages_en_US` (không có) $\rightarrow$ `Messages_en` (CÓ key `greeting`).
  Do đó Java sẽ lấy giá trị từ `Messages_en.properties` trước khi phải lùi về bundle gốc `Messages.properties`.

