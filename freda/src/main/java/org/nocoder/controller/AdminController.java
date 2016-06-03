package org.nocoder.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nocoder.model.Article;
import org.nocoder.model.User;
import org.nocoder.service.ArticleService;
import org.nocoder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {
	
	@Resource
	private UserService userService;
	@Autowired
	private ArticleService articleService;

	@RequestMapping({ "/admin.html" })
	public String toAdmin(HttpServletRequest request, Model model) {
		if (request.getSession().getAttribute("user") != null) {

			final String tag = request.getParameter("tag");
			final Integer page = Integer.valueOf(Integer.parseInt(request.getParameter("page") == null ? "1" : request.getParameter("page")));
			final Integer pageSize = Integer.valueOf(5);
			final Object[] result = queryArticlesByPage(tag, page, pageSize);

			@SuppressWarnings("unchecked")
			final List<Article> articleList = (List<Article>) result[0];
			model.addAttribute("articleList", articleList);
			model.addAttribute("page", page);
			model.addAttribute("totalPages", result[1]);

			return "admin/admin";
		}
		return "redirect:login.html";
	}

	@RequestMapping({ "/login.html" })
	public String toIndex(HttpServletRequest request, Model model) {
		if (request.getSession().getAttribute("user") != null) {
			return "redirect:admin.html";
		}
		final String username = request.getParameter("username");
		final String password = request.getParameter("password");
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			final User user = this.userService.queryUserByNameAndPwd(username, password);
			if (user != null) {
				model.addAttribute("user", user);
				request.getSession().setAttribute("user", user);
				return "redirect:admin.html";
			}
		}
		return "admin/login";
	}

	@RequestMapping({ "/editor.html" })
	public String toCFEditor() {
		
		return "admin/editor";
	}

	private Object[] queryArticlesByPage(String tag, Integer page, Integer pageSize) {
		final Object[] result = new Object[2];
		if (page == null) {
			page = Integer.valueOf(1);
		}
		if (pageSize == null) {
			pageSize = Integer.valueOf(10);
		}
		final int articlesCount = this.articleService.countArticles(tag);
		if (articlesCount > 0) {
			List<Article> articleList = this.articleService.queryArticleList(tag, Integer.valueOf((page.intValue() - 1) * pageSize.intValue()), pageSize);
			result[0] = articleList;
			// 总页数 取天花板值
			int totalPages = (int) Math.ceil((double) articlesCount / (double) pageSize.intValue());
			result[1] = Integer.valueOf(totalPages);
		}
		return result;
	}


	@RequestMapping({ "/article/save.html" })
	public String saveArticle(HttpServletRequest request, Model model, Article article) {
		this.articleService.saveArticle(article);
		return "redirect:/index.html";
	}

	@RequestMapping({ "/article/edit.html" })
	public String toEdit(HttpServletRequest request,  String id, Model model) {
		Article article = this.articleService.queryArticleById(id);
		model.addAttribute("article", article);
		return "admin/editor";
	}
}
