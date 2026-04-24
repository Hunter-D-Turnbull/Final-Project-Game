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

After you sync your gradle, he next (and hopefully final) thing you should need to do before you can use Firestore and Firebase is go to the Firebase console for your app and scroll down on the left side to databases and storage. If you hover over it you should see NoSQL and Firestore as an option. Click on it, and create the database.
**KEEP THE NAME/ID AS (default) OR YOU WILL HAVE ACCESS ISSUES**

With all of this set up, you should be ready to move on to the coding section.

## Actual Code for Firebase and Firestore

The code to use Firebase and Firestore are both pretty small and simple. I'm not going to show you exactly how to implement the functions to do so into your game, but I will show you what the functions should look like in your AppViewModel. One thing to always keep in mind is to make sure you are always using .await() in a try/catch block for this. Other methods can and will give you errors.

The first thing you should add is a way for users to sign up with their name, along with an email and password.

```kotlininit {
    init {
        auth.addAuthStateListener(authListener) // Needs to go in your view model init for all future functions
    }
    fun signUp(
        email: String,
        password: String,
        name: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("User creation failed")

                val userData = mapOf(
                    "name" to name,
                    "email" to email
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .await()

                onResult(true, null)

            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
```

After people can create their account, they will want to sign in, so the next thing you should add is a function to sign in with.

```kotlin
    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
```

And obviously, if people can sign in, they should also be able to sign out.

```kotlin
    fun signOut() {
        auth.signOut()
    }
```

All pretty simple right? If you wanted to, you can also add a way for users to make changes to their account, like changing their password or deleting their account, but it's not necessary to get everything up and running, so I'm skipping over those for now. You can find the functions I used to do so in my code if you do want to add them though.

Everything is great so far, but we still don't have a way to store actual game history or any of the achievements players have made. The first issue of these two we'll focus on is storing game data for users. There may be better ways to do it, but the way I opted for was "starting game sessions" and saving the game/hand data after the game session ended. Below are the functions I used to start and end game sessions.

```kotlin
    suspend fun startRemoteGameSession() {
        val uid = auth.currentUser?.uid ?: return

        try {
            val gameData = mapOf(
                "totalHands" to 0,
                "finalPoints" to _currentPoints.value,
                "timestamp" to System.currentTimeMillis()
            )

            val docRef = firestore.collection("users")
                .document(uid)
                .collection("history")
                .add(gameData)
                .await()

            currentRemoteGameId = docRef.id

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun endRemoteGameSession() {
        val uid = auth.currentUser?.uid ?: return
        val gameId = currentRemoteGameId ?: return

        viewModelScope.launch {
            try {
                val updatedGame = mapOf(
                    "totalHands" to (_totalHandsPlayed.value - 1),
                    "finalPoints" to _currentPoints.value
                )

                firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .document(gameId)
                    .update(updatedGame)
                    .await()
                currentRemoteGameId = null

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
```

You're probably asking yourself, "What good are these stupid game sessions without the code to save the hand and game data?" and the answer would be no good at all. However, you may have actually noticed that endRemoteGameSession() actually saves remote game history, so you just need a function that saves an individual hand when called. Below is the code to do that.

```kotlin
    fun saveRemoteHand(hand: Hand, handNumber: Int) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Wait up to ~1 second for gameId to be ready
            var attempts = 0
            while (currentRemoteGameId == null && attempts < 10) {
                kotlinx.coroutines.delay(100)
                attempts++
            }

            val gameId = currentRemoteGameId
            if (gameId == null) {
                println("Still no gameId after waiting — skipping save")
                return@launch
            }
            val dealerScore = _currentDealerTotal.value

            val result = when {
                hand.score > 21 -> "LOSS"
                dealerScore > 21 -> "WIN"
                hand.score > dealerScore -> "WIN"
                hand.score < dealerScore -> "LOSS"
                else -> "PUSH"
            }

            try {
                val handData = mapOf(
                    "handNumber" to handNumber,
                    "result" to result,
                    "pointsChange" to _currentBet.value
                )

                firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .document(gameId)
                    .collection("hands")
                    .add(handData)
                    .await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
```

Now we have the functions to save the code, but the no functions to allows the user to access their own scores. That doesn't seem quite fair, so below are the functions that allows the users to access both their hand and game history. It's entirely up to you to choose how to implement it, I personally used a scrollable list with clickable cards for each game, that when clicked, would access the hand breakdown for that game

```kotlin
    fun fetchRemoteGames() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .get()
                    .await()

                val games = snapshot.documents.map { doc ->
                    RemoteGame(
                        id = doc.id,
                        finalPoints = doc.getLong("finalPoints")?.toInt() ?: 0,
                        totalHands = doc.getLong("totalHands")?.toInt() ?: 0,
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }.sortedByDescending { it.timestamp }

                _remoteGames.value = games

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRemoteHands(gameId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("history")
                    .document(gameId)
                    .collection("hands")
                    .get()
                    .await()

                val hands = snapshot.documents.map { doc ->
                    RemoteHand(
                        handNumber = doc.getLong("handNumber")?.toInt() ?: 0,
                        pointsChange = doc.getLong("pointsChange")?.toInt() ?: 0,
                        result = doc.getString("result") ?: "UNKNOWN"
                    )
                }.sortedBy { it.handNumber }

                _remoteHands.value = hands

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
```

Look at us, go we made a way for users to sign into the game with their email, and have their game history stored so they can look at it whenever they want. Personally though, I think only having access too game history is a little boring. What good is a game if it doesn't have achievements for it's users? Let's add some. Below is the code that initializes the achievements. I put these in their own file, seperate from viewModel, but you can also just put them in the same file outside of the AppViewModel if you want to.

```kotlin
data class Achievement(
    val id: String,
    val title: String,
    val description: String
)

val achievementsList = listOf(
    Achievement("play_1", "Getting Started", "Play 1 game"),
    Achievement("play_5", "Regular", "Play 5 games"),
    Achievement("play_10", "Addicted", "Play 10 games"),

    Achievement("points_2500", "Stacking Chips", "Reach 2500 points"),
    Achievement("points_5000", "High Roller", "Reach 5000 points"),
    Achievement("points_0", "Broke", "Reach 0 points"),

    Achievement("win_1", "First Win", "Win 1 hand"),
    Achievement("win_5", "On a Roll", "Win 5 hands"),
    Achievement("win_10", "Unstoppable", "Win 10 hands"),

    Achievement("blackjack", "Blackjack!", "Get a 21"),

    Achievement("all_in", "All In", "Bet all your points"),
    Achievement("double_down", "Double Down", "Use double down"),
    Achievement("split", "Split Decision", "Split a hand"),
    Achievement("stand", "Stand Firm", "Stand on a hand"),
    Achievement("hit", "Hit Me", "Hit a card")
)
```

Now we need to track achievement progress for users, and update both the progress and achievements accordingly. We don't want achievement progress to be tracked locally, because then it would reset everytime our device restarted. Instead, we use Firestore collections to track achievements and progress per user in the functions below.

```kotlin
    fun fetchStats() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val doc = firestore.collection("users")
                    .document(uid)
                    .collection("stats")
                    .document("global")
                    .get()
                    .await()

                _gamesPlayed.value = doc.getLong("gamesPlayed")?.toInt() ?: 0
                _totalWins.value = doc.getLong("totalWins")?.toInt() ?: 0

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateStats(gamesDelta: Int = 0, winsDelta: Int = 0) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val ref = firestore.collection("users")
                    .document(uid)
                    .collection("stats")
                    .document("global")

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(ref)

                    val currentGames = snapshot.getLong("gamesPlayed") ?: 0
                    val currentWins = snapshot.getLong("totalWins") ?: 0

                    transaction.set(
                        ref,
                        mapOf(
                            "gamesPlayed" to currentGames + gamesDelta,
                            "totalWins" to currentWins + winsDelta
                        )
                    )
                }.await()

                // update local cache
                _gamesPlayed.value += gamesDelta
                _totalWins.value += winsDelta

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unlockAchievement(achievementId: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val ref = firestore.collection("users")
                    .document(uid)
                    .collection("achievements")
                    .document(achievementId)

                val doc = ref.get().await()
                if (doc.exists()) return@launch

                ref.set(
                    mapOf(
                        "unlocked" to true,
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun fetchUnlockedAchievements() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(uid)
                    .collection("achievements")
                    .get()
                    .await()

                _unlockedAchievements.value =
                    snapshot.documents.map { it.id }.toSet()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
```

fetchUnlockedAchievements() is the function used to actually allow the user to access their own achievements. I had them on their own screen with a scrollable list, but you can implement this feature however you want.

## Further Discussion

Congratulations, you should now have everything you need to use Firebase and Firestore to make a blackjack game with user data storage and achievements. You can obviously make it more advanced if you want and use my actual project code as an example. A few things that are in my actual project that I didn't go over were a way for users to change their password, delete their account, and wipe their game history. I also added local game storage and a global leaderboard for high scores among users on the game, but those seemed like a lot to go over, and at the end I would have given you my entire program, so I opted out of doing that.

## Helpful links

Firebase: https://firebase.google.com/  
Google Cloud Services: https://cloud.google.com/
Firestore Intro and Tutorial: https://firebase.google.com/docs/firestore
Firebase Fundamentals Guide: https://firebase.google.com/docs/guides
Firebase Tutorial Video: https://www.youtube.com/watch?v=_L8j-ZC83y4
