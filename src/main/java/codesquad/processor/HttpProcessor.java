package codesquad.processor;

import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public interface HttpProcessor {
    HttpRequest parseRequest(BufferedReader br) throws IOException;

    void writeResponse(OutputStream os, HttpResponse response) throws IOException;
}
