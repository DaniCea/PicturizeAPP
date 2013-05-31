				Picturize
Picturize is an APP designed for reviewing, uploading and downloading photos from Facebook albums. The functions of the APP are:
	- Login and logout into Facebook account
	- Review of user albums
	- Review of all the pictures inside albums (include caching of images if SD card is 	 	available, which makes the APP need only once to download the pictures).
	- Review of a single picture, including pinch and zoom (also includes caching).
	- Taking a picture and uploading to Facebook
	- Download Facebook pictures to your mobile (SD card needed)

There is nothing missing respect to my original plan, but there are some points that could be improved:
	- The APP has no progress bars while uploading/downloading the images, which would 	make it much more comfortable for users.
	- All the images are downloaded at the same time using threads, which makes images 	to load in a random order and may cause a little bit confusion to the users.
	- Images taken from the camera are always uploaded to facebook in Landscape 	orientation.

To do this project, I've used the following libraries:
Android Universal Image Loader: https://github.com/nostra13/Android-Universal-Image-Loader
Facebook SDK: https://developers.facebook.com/android/