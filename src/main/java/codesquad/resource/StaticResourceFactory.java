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
                "</li>" +
                "<li class=\"header__menu__item\">\n" +
                "    <a class=\"btn btn_ghost btn_size_s\" href=\"/article\">글쓰기</a>\n" +
                "</li>", userName);
    }

    public static String NO_POSTS = "<div class=\"post__no-posts\">\n" +
            "<h1>텅~~!</h1>" +
            "    <div>작성된 게시글이 없습니다.</div>\n" +
            "</div>";

    public static String NEED_LOGIN = "<div class=\"post__no-posts\">\n" +
            "<h1>로그인이 필요합니다.</h1>" +
            "    <div>로그인 후 이용해주세요.</div>\n" +
            "</div>";
}
