<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>로그인하기</title>
    <link rel="stylesheet" href="/css/table.css"/>
    <script type="text/javascript" src="/js/jquery-3.6.0.min.js"></script>
    <script type="text/javascript">

        $(document).ready(function () {

            $("#btnUserReg").on("click", function () {
                location.href = "/user/userRegForm";
            })

            $("#btnSearchUserId").on("click", function () {
                location.href = "/user/searchUserId";
            })

            $("#btnSearchPassword").on("click", function () {
                location.href = "/user/searchPassword";
            })

            $("#btnLogin").on("click", function () {
                let f = document.getElementById("f");

                if (f.userId.value === "") {
                    alert("아이디를 입력하세요.");
                    f.userId.focus();
                    return;
                }

                if (f.password.value === "") {
                    alert("비밀번호를 입력하세요.");
                    f.password.focus();
                    return;
                }

                $.ajax({
                    url: "/user/loginProc",
                    type: "post",
                    dataType: "JSON",
                    data: $("#f").serialize(),
                    success: function (json) {

                        if (json.result === 1) {
                            alert(json.msg);
                            location.href = "/user/loginResult";

                        } else {
                            alert(json.msg);
                            $("#userId").focus();
                        }
                    }
                })
            })
        })
    </script>
</head>
<body>
<h2>로그인하기</h2>
<hr/>
<br/>
<form id="f">
    <div class="divTable minimalistBlack">
        <div class="divTableBody">
            <div class="divTableRow">
                <div class="divTableCell">아이디
                </div>
                <div class="divTableCell">
                    <input type="text" name="userId" id="userId" style="width:95%"/>
                </div>
            </div>
            <div class="divTableRow">
                <div class="divTableCell">비밀번호
                </div>
                <div class="divTableCell">
                    <input type="password" name="password" id="password" style="width:95%"/>
                </div>
            </div>
        </div>
    </div>
    <div>
        <button id="btnLogin" type="button">로그인</button>
        <button id="btnUserReg" type="button">회원가입</button>
        <button id="btnSearchUserId" type="button">아이디 찾기</button>
        <button id="btnSearchPassword" type="button">비밀번호 찾기</button>
    </div>
</form>
</body>
</html>
