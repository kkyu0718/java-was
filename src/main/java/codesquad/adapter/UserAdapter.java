package codesquad.adapter;

import codesquad.db.UserDb;
import codesquad.global.Path;
import codesquad.http.*;
import codesquad.model.User;

public class UserAdapter implements Adapter {
    @Override
    public boolean supports(Path path) {
        return path.toString().startsWith("/user");
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        if (request.getPath().toString().equals("/user/create")) {
            Parameters parameters = request.getParameters();
            String userId = parameters.getParameter("userId");
            String password = parameters.getParameter("password");
            String name = parameters.getParameter("name");
            String email = parameters.getParameter("email");

            User user = User.of(userId, password, name, email);

            UserDb.add(user);
            return new HttpResponse(request, HttpStatus.OK, new HttpHeaders(), null);
        }

        throw new IllegalArgumentException("처리 가능한 메소드가 존재하지 않습니다." + request.getPath().toString());
    }
}
