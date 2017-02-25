package masterSpringMvc.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import masterSpringMvc.config.PictureUploadProperties;

@Controller
@SessionAttributes("picturePath")
public class PictureUploadController {
	private final Resource picturesDir;
	private final Resource anonymousPicture;
	private final MessageSource messageSource;
	private final UserProfileSession userProfileSession;

	@Autowired
	public PictureUploadController(PictureUploadProperties uploadProperties, MessageSource messageSource,
			UserProfileSession userProfileSession) {
		picturesDir = uploadProperties.getUploadPath();
		anonymousPicture = uploadProperties.getAnonymousPicture();
		this.messageSource = messageSource;
		this.userProfileSession = userProfileSession;
	}

	@RequestMapping(value = "/profile", params = { "upload" }, method = RequestMethod.POST)
	public String onUpload(@RequestParam MultipartFile file, RedirectAttributes redirectAttrs) throws IOException {
		if (file.isEmpty() || !isImage(file)) {
			redirectAttrs.addFlashAttribute("error", "Niewłaściwy plik. Załaduj plik z obrazem.");
			return "redirect:/profile";
		}
		Resource picturePath = copyFileToPictures(file);
		userProfileSession.setPicturePath(picturePath);
		return "redirect:/profile";
	}

	@RequestMapping(value = "/uploadedPicture")
	public void getUploadedPicture(HttpServletResponse response) throws IOException {
		Resource picturePath = userProfileSession.getPicturePath();
		if (picturePath == null) {
			picturePath = anonymousPicture;
		}
		response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(picturePath.getFilename()));
		IOUtils.copy(picturePath.getInputStream(), response.getOutputStream());
	}

	@RequestMapping("uploadError")
	public ModelAndView onUploadError(Locale locale) {
		ModelAndView modelAndView = new ModelAndView("profile/profilePage");
		modelAndView.addObject("error", messageSource.getMessage("upload.file.too.big", null, locale));
		modelAndView.addObject("profileForm", userProfileSession.toForm());
		return modelAndView;
	}

	@ExceptionHandler(IOException.class)
	public ModelAndView handleIOException(Locale locale) {
		ModelAndView modelAndView = new ModelAndView("profile/profilePage");
		modelAndView.addObject("error", messageSource.getMessage("upload.io.exception", null, locale));
		modelAndView.addObject("profileForm", userProfileSession.toForm());
		return modelAndView;
	}

	private Resource copyFileToPictures(MultipartFile file) throws IOException {
		String fileExtension = getFileExtension(file.getOriginalFilename());
		File tempFile = File.createTempFile("pic", fileExtension, picturesDir.getFile());
		try (InputStream in = file.getInputStream(); OutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(in, out);
		}
		return new FileSystemResource(tempFile);
	}

	private boolean isImage(MultipartFile file) {
		return file.getContentType().startsWith("image");
	}

	private static String getFileExtension(String name) {
		return name.substring(name.lastIndexOf("."));
	}
}
