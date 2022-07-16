### RESTful API : 도서 관리 서비스

<br>

<hr>

### [서비스 개요]
* 누구나 쉽고 간단하게 도서관리 서비스를 개발할 수 있도록 사용이 간편하게 제작된 RESTful API 서비스.
* 관리자뿐만 아니라 일반 사용자로서 사용가능한 도서 예약신청 및 대출현황 조회, 리뷰 작성, 메세지 송수신 등의 커뮤니티 기능을 제공하는 서비스.

<br>

<hr>

### [서비스 목적]
* [기존 RESTful API 개발 경험](https://github.com/Blanc-et-noir/RestAPI)을 바탕으로 좀 더 체계적으로 구성된 서버를 구축해보는 것.
* 기존의 뷰를 직접 구성하여 수행하는 테스트 방법 대신, Postman을 활용하여 자동화된 테스트 수행방법을 채택하고 익히는 것.
* 뷰를 담당할 다른 개발자와 협업하여 두 시스템간의 통합을 이뤄보는 것.
* 기존에 사용하지 않았던 Spring Framework의 여러가지 기능을 배우고 익히는 것.

<br>

<hr>

### [서비스 특징]
* CORS 기능을 지원하여 외부에서 별다른 설정없이 바로 API 사용이 가능함.
* 요청 및 응답헤더에 JWT Token을 첨부하여 사용자를 인증하고, 사용자 정보를 세션 객체로 저장할 필요가 없어 더 많은 사용자를 수용할 것으로 기대.
* HTTPS 프로토콜을 사용하여 페이로드 및 헤더를 암호화함. (이전에 개발했던 서비스에서는 HTTP 프로토콜을 사용)

<br>

<hr>

### [사용자 요구사항]
   
<details>
<summary>회원가입 요구사항</summary>

<br>

* **[기능]** 사용자는 회원가입시 아이디, 비밀번호, 이름, 전화번호, 비밀번호 찾기 질문, 비밀번호 찾기 질문 답을 제공해야함.
* **[기능]** 회원가입시 기본 10마일리지 제공.

<br>

* **[비기능]** 비밀번호, 비밀번호 찾기 질문에 대한 답과 같은 민감한 정보 전송시 RSA2048 암호화 사용, 복호화 키는 Redis에 저장. [임시 보류]
* **[비기능]** 또한 민감한 정보는 SHA512 및 SALT를 활용하여 단방향 암호화하여 DB에 저장.

</details>

<br>

<details>
<summary>로그인 요구사항</summary>

<br>

* **[기능]** 사용자는 회원가입시 아이디, 비밀번호, 이름, 전화번호, 비밀번호 찾기 질문, 비밀번호 찾기 질문 답을 제공해야함.
* **[기능]** 로그인시 하루에 한 번만 마일리지 1점을 부여함.

<br>

* **[비기능]** 비밀번호는 암호화하여 서버로 전송, SHA512 및 SALT로 해싱하여 인증에 성공하면 액세스, 리프레쉬 토큰 반환. [임시 보류]
* **[비기능]** 액세스 토큰의 기한은 1시간, 리프레쉬 토큰의 기한은 14일.

</details>

<br>

<details>
<summary>회원탈퇴 요구사항</summary>

<br>

* **[기능]** 사용자는 언제든지 회원탈퇴 가능. 단, 현재 대출중인 도서가 없어야함.
* **[기능]** 메세지, 리뷰 등은 탈퇴한 회원이 작성한 것으로 취급함.

</details>

<br>

<details>
<summary>도서 대출 요구사항</summary>

<br>

* **[기능]** 한 사람당 최대 동시에 3권 대여 가능
* **[기능]** 대출기한은 2주, 기한연장은 최대 2번 가능.
* **[기능]** 도서의 재고에 비해 대출을 예약한 사람이 많으면 대출 기한 연장은 불가능.
* **[기능]** 연체로 인해 대출이 불가할 경우, 1일당 3마일리지를 소모하여 대출가능시각을 앞당길 수 있음.

</details>

<br>

<details>
<summary>도서 예약 요구사항</summary>

<br>

* **[기능]** 한 사람당 최대 3권 도서 예약 가능. 연체로 인해 대출이 불가능할 경우에는 예약불가.
* **[기능]** 하나의 책당 최대 5명 예약 가능.
* **[기능]** 예약된 책이 반납되면, 예약한 사람중 가장 먼저 예약한 사람에게 대출 우선권이 부여됨.
* **[기능]** 대출을 예약한 시점을 기준으로 7일이내에 예약이 자동으로 사라짐.

</details>

<br>

<details>
<summary>도서 반납 요구사항</summary>

<br>

* **[기능]** 반납시 연체한 일수만큼 도서대출 가능 날짜가 뒤로 미뤄짐.
* **[기능]** 현재 대출 가능한 시각과 반납날짜중 더 최신의 날짜 + 연체일수로 계산함.
* **[기능]** 도서 연체시 연체일수당 마일리지 3점을 감소시킴.
* **[기능]** 7일 이상 대출한 도서 정상반납시에 마일리지 2점을 부여함.

</details>

<br>

<details>
<summary>추후작성</summary>

<br>

* **[기능]** 추후작성

</details>

<br>

<details>
<summary>추후작성</summary>

<br>

* **[기능]** 추후작성

</details>

<br>

<details>
<summary>추후작성</summary>

<br>

* **[기능]** 추후작성

</details>

<br>

<details>
<summary>추후작성</summary>

<br>

* **[기능]** 추후작성

</details>

<br>

<details>
<summary>추후작성</summary>

<br>

* **[기능]** 추후작성

</details>
<hr>

### [기술 스택]
1. Java
2. Spring Framework
3. Redis
4. Maria DB
5. GIT
6. JWT Token

<br>

<hr>

### [API 명세서]
* [명세서 확인하기](https://documenter.getpostman.com/view/21751638/UzJETzE2)
