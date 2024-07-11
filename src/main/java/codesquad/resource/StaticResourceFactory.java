package codesquad.resource;

public class StaticResourceFactory {
    private StaticResourceFactory() {
    }

    public static String GUEST_GREETING = "<li class=\"header__menu__item\">\n" +
            "    <a class=\"btn btn_contained btn_size_s\" href=\"/login\">로그인</a>\n" +
            "</li>\n" +
            "<li class=\"header__menu__item\">\n" +
            "    <a class=\"btn btn_ghost btn_size_s\" href=\"/registration\">회원 가입</a>\n" +
            "</li>";

    public static String getUserGreeting(String userName) {
        return String.format("<li class=\"header__menu__item\">\n" +
                "    <div>안녕하세요 %s</div>\n" +
                "</li>", userName);
    }
}
