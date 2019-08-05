package com.offcn.shop.controller;

import com.offcn.entity.Result;
import com.offcn.utli.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;

	@RequestMapping("/upload")
	public Result upload(MultipartFile file){
		String originalFilename = file.getOriginalFilename();
		String substring = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
		try {
			FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
			String path = fastDFSClient.uploadFile(file.getBytes(), substring);
			String url = FILE_SERVER_URL + path;
			return new Result(true,url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "上传失败");
		}
	}

}
