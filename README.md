## Presentation and Demo  

Slides: https://docs.google.com/presentation/d/1aNjUobzs_cJ3CjVXhvGqS1A4iADqmK32PPTtn7hWCBA/edit?usp=sharing  
Video Presentation: https://youtu.be/CgKu-ca4yhI  
  
## Tutorial for Firebase and Firestore  

Firebase is a service from Google that provides developers with data storage and user management needed to create apps.
Firestore is a nosql database that stores data in collections through Google Cloud Services, and directly links to Firebase.
Both of these tools combined are all you should need to create a simple Blackjack game with user authentication and data storage.

## Getting Started

You'll need a few things before you can begin coding. First, you should have an existing repository that you can link to Firebase. You need to use the package name to link it, so keep that in mind.
After you make your repository, make your way to the Firebase website, which you should be able to find by just searching it in Google. Make an account, then click go to console in the top right corner of the webiste.
Work your way through the steps it gives you, importing all required files and dependencies on the way. Make sure your put the .json file in the correct location.

At this point, you should already have a couple of the needed plugins and dependencies, but you'll need more to have access to Firestore. Below is everything you should need.

Top-Level:
```kotlin
plugins {
  id("com.google.gms.google-services") version "4.4.4" apply false
}
```

App-Level:
```kotlin
plugins {
  id("com.google.gms.google-services")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
}
```

