package br.com.hobbyhub.jsf.config;

import br.com.hobbyhub.jsf.service.HobbyHubService;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AcessoFilter implements Filter {

    @Inject
    private HobbyHubService api;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest requisicao = (HttpServletRequest) request;
        HttpServletResponse resposta = (HttpServletResponse) response;
        String caminho = requisicao.getServletPath();
        if ((caminho.equals("/feed.xhtml") || caminho.equals("/postar.xhtml")
                || caminho.startsWith("/midias/")) && !api.isAutenticado()) {
            if (caminho.startsWith("/midias/")) resposta.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            else resposta.sendRedirect(requisicao.getContextPath() + "/login.xhtml");
            return;
        }
        chain.doFilter(request, response);
    }
}
