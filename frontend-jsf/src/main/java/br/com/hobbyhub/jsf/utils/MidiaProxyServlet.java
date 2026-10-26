package br.com.hobbyhub.jsf.utils;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/midias/*")
public class MidiaProxyServlet extends HttpServlet {

    @Inject
    private HobbyHubApiClient api;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nome = request.getPathInfo() == null ? "" : request.getPathInfo().substring(1);
        String range = request.getHeader("Range");
        try {
            var midia = api.carregarMidia(nome, range);
            response.setStatus(midia.statusCode());
            for (String header : List.of("Content-Type", "Content-Range", "Accept-Ranges")) {
                midia.headers().firstValue(header).ifPresent(valor -> response.setHeader(header, valor));
            }
            response.setHeader("Cache-Control", "private, no-store");
            response.getOutputStream().write(midia.body());
        } catch (ApiException erro) {
            response.sendError(erro.getStatus());
        }
    }
}
