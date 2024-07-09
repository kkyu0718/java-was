package codesquad.adapter;

import codesquad.db.UserDb;
import codesquad.http.HttpBody;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.http.Parameters;
import codesquad.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAdapter implements Adapter {
    private static final Logger logger = LoggerFactory.getLogger(UserAdapter.class);

    @Override
    public boolean supports(String path) {
        return path.startsWith("/user");
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        if (request.getPath().equals("/user/create")) {
            HttpBody body = request.getBody();

            Parameters parameters = body.getParameters();
            String userId = parameters.getParameter("userId");
            String password = parameters.getParameter("password");
            String name = parameters.getParameter("name");
            String email = parameters.getParameter("email");
            UserDb.add(User.of(userId, password, name, email));

            UserDb.print();
            return HttpResponse.createRedirectResponse(request, "/index.html");
        }

        throw new IllegalArgumentException("처리 가능한 메소드가 존재하지 않습니다." + request.getPath().toString());
    }
}
