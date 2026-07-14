<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="kopo.poly.dto.UserInfoDTO" %>
<%@ page import="kopo.poly.util.CmmUtil" %>
<%
  UserInfoDTO rDTO = (UserInfoDTO) request.getAttribute("rDTO");

  String msg = "";

  if (CmmUtil.nvl(rDTO.getUserId()).length() > 0) {
    msg = CmmUtil.nvl(rDTO.getUserName()) + " 회원님의 " + CmmUtil.nvl(rDTO.getUserId()) + "입니다.";

  } else {
    msg = "아이디가 존재하지 않습니다.";
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title><%=msg%></title>
  <link rel="stylesheet" href="/css/table.css"/>
  <script type="text/javascript" src="/js/jquery-3.6.0.min.js"></script>
  <script type="text/javascript">

    $(document).ready(function () {

      $("#btnLogin").on("click", function () {
        location.href = "/user/login";
      })
    })
  </script>
</head>
<body>
<h2>아이디 찾기 결과</h2>
<hr/>
<br/>
<form id="f">
  <div class="divTable minimalistBlack">
    <div class="divTableBody">
      <div class="divTableRow">
        <div class="divTableCell">
          <%=msg%>
        </div>
      </div>
    </div>
  </div>
  <div>
    <button id="btnLogin" type="button">로그인</button>
  </div>
</form>
</body>
</html>
