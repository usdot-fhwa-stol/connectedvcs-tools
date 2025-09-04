# S3 Bucket Configuration Guide

This guide explains how to configure S3 integration for the `fedgov-cv-map-services-proxy` application by updating the `application.properties` file and creating an S3 bucket.

---

## 1. Create an S3 Bucket

1. **Log in to the AWS Management Console.**
2. **Navigate to S3** from the AWS services menu.
3. **Click "Create bucket".**
4. **Enter a unique bucket name** (e.g., `my-fedgov-cv-map-tiles`).
5. **Select a region** (e.g., `us-east-1`).  
   _Note: The region must match the value you will set in the properties file._
6. **Click "Create bucket".**

---

## 2. Obtain AWS Credentials

You need an AWS Access Key ID and Secret Access Key with permissions to access the bucket.

- Go to **IAM > Users** in the AWS Console.
- Select or create a user.
- Attach the policy `AmazonS3FullAccess` (or a more restrictive policy as needed).
- Generate and note the **Access Key ID** and **Secret Access Key**.

---

## 3. Update `application.properties`

Open `fedgov-cv-map-services-proxy/src/main/resources/application.properties` and add or update the following properties:

```properties
# S3 Configuration
aws.s3.accessKey=YOUR_AWS_ACCESS_KEY_ID
aws.s3.secretKey=YOUR_AWS_SECRET_ACCESS_KEY
aws.s3.region=YOUR_AWS_REGION   # e.g., us-east-1
aws.s3.bucket=YOUR_BUCKET_NAME  # e.g., my-fedgov-cv-map-tiles