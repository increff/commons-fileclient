# commons-fileclient
This library is used for storage and retrieval of documents from cloud storage platforms such as Google Cloud Storage, Amazon S3 etc.

- [commons-lang](#commons-lang)
    * [Installation](#installation)
    * [Key Classes](#key-classes)
        + [Example](#example)
    * [License](#license)
 
## Installation
To add a dependency on this library using Maven, use the following:
```xml
<dependency>
    <groupId>com.increff.commons</groupId>
    <artifactId>commons-fileclient</artifactId>
    <version>${increff-commons-fileclient.version}</version>
</dependency>
```

## Key Classes
### AbstractFileProvider

An abstract class called `AbstractFileProvider` contains the methods to create, read, delete and write objects from a storage. These methods are then implemented by the classes that perform these operations on different storage platforms such as

1. `GcpFileProvider` for Google Cloud
2. `AwsFileProvider` for Amazon
3. `NativeFileProvider` for local, on-device folders
4. `FileClient`

The file client is what is made available to the user for interacting with the storage platform. FilecCient internally makes use of one of the mentioned storage providers. Depending on the provider, appropriate information has to be passed to FileClient before using it. For example, for using Amazon S3, information such as AWS Bucket name and AWS Bucket URL have to be passed to FileClient. The following methods are available for manipulations on files:

- `void create(String filePath, InputStream is)`: Create or write the data from the InputStream to specified file path
- `void delete(String filePath)`: Delete the file stored at specified file path
- `String getReadUri(String filePath)`: Return the URL/path to the file. E.g. for S3, returns the AWS Bucket URL with the file path
- `String getWriteUri(String filePath)`: Return URL/path for writing
- `void writeToUri(String uploadUri, InputStream is)`: Write data to storage from input stream by doing a POST request at the uploadUri
### Example

To interact with, say Amazon S3 storage, we use the appropriate implementation of AbstractFileProvider. For S3, this implementation is under the class AwsFileProvider. Using the implementation, we can create a FileClient as follows
```java
AbstractFileProvider provider = new AwsFileProvider(awsRegion, awsAccessKey, awsSecretKey, awsBucketName,awsBucketUrl);
FileClient fileClient = new FileClient(provider);
```

Subsequently, we can interact with the storage by calling the methods of FileClient as follows
```java
fileClient.create(filePath, inputStream);
fileClient.delete(filePath);
```
## License
Copyright (c) Increff

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.
