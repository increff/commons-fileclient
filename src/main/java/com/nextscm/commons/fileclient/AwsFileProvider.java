package com.nextscm.commons.fileclient;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AwsFileProvider extends AbstractFileProvider {

	// 30 Seconds
	static final int duration = 30 * 1000;
	static final HttpMethod httpMethod = HttpMethod.PUT;

	private final AmazonS3 s3;
	private final String awsBucketName;
	private final String awsBucketUrl;

	public AwsFileProvider(String awsRegion, String awsAccessKey, String awsSecretKey, String awsBucketName,
			String awsBucketUrl) {
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		BasicAWSCredentials creds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		builder.withRegion(Regions.fromName(awsRegion));
		builder.setCredentials(new AWSStaticCredentialsProvider(creds));
		s3 = builder.build();
		this.awsBucketName = awsBucketName;
		this.awsBucketUrl = awsBucketUrl;
	}

	@Override
	public void create(String filePath, InputStream is) {
		ObjectMetadata om = new ObjectMetadata();
		s3.putObject(awsBucketName, filePath, is, om);
	}

	@Override
	public List<ObjectSummary> getAllObjects(String directory) {
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(awsBucketName)
				.withPrefix(directory + "/");
		List<ObjectSummary> objectSummaryList = new ArrayList<>();
		ObjectListing objectListing = s3.listObjects(listObjectsRequest);
		while (objectListing.isTruncated()) {
			objectSummaryList.addAll(objectListing.getObjectSummaries().stream().map(os -> getObjectSummary(os))
					.collect(Collectors.toList()));
			objectListing = s3.listNextBatchOfObjects(objectListing);
		}
		objectSummaryList.addAll(objectListing.getObjectSummaries().stream().map(os -> getObjectSummary(os))
				.collect(Collectors.toList()));
		return objectSummaryList;
	}

	@Override
	public String getReadUri(String filePath) {
		return awsBucketUrl + "/" + filePath;
	}

	@Override
	public void delete(String filePath) {
		s3.deleteObject(awsBucketName, filePath);
	}

	@Override
	public String getWriteUri(String filePath) {
		Date expiration = Utils.getExpirationDate(duration);
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(awsBucketName,
				filePath);
		generatePresignedUrlRequest.setMethod(httpMethod);
		generatePresignedUrlRequest.setExpiration(expiration);
		URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);
		return url.toString();
	}

	@Override
	public InputStream get(String filePath) {
		return s3.getObject(new GetObjectRequest(awsBucketName, filePath)).getObjectContent();
	}

	// HELPER METHOD
	private static ObjectSummary getObjectSummary(S3ObjectSummary os) {
		ObjectSummary objectSummary = new ObjectSummary();
		objectSummary.setBucketName(os.getBucketName());
		objectSummary.setKey(os.getKey());
		objectSummary.setLastModified(os.getLastModified());
		objectSummary.setSize(os.getSize());
		return objectSummary;
	}
}
