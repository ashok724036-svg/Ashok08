# NEETQuestSaver — APK Build Steps (GitHub Actions)

Yeh project GitHub Actions par automatic build ho jayega aur APK download karne ko milegi.

## Step-by-step (mobile se bhi ho sakta hai)

1. **GitHub par naya repo banao**
   - https://github.com/new
   - Repository name: `NEETQuestSaver` (kuch bhi rakh sakte ho)
   - Public ya Private — dono chalega
   - "Create repository" dabao

2. **Code upload karo**
   - Repo ke page par "uploading an existing file" link dabao
   - Is zip ke andar ki **saari files aur folders** drag-drop karo
     (zip extract karke, andar ki files upload karna, zip khud nahi)
   - Niche "Commit changes" dabao

3. **Build automatic chalu ho jayega**
   - Repo ke top par **Actions** tab kholo
   - "Build Android APK" naam ka workflow chalta dikhega (yellow dot)
   - 5–8 minute lagenge pehli baar (Gradle download hota hai)
   - Green tick aane ka wait karo

4. **APK download karo**
   - Us successful run par click karo
   - Page ke niche **Artifacts** section me "NEETQuestSaver-debug-apk" dikhega
   - Use tap karke download kar lo (zip aayega, andar APK file hogi)

5. **Phone me install karo**
   - Zip extract karo, APK file kholo
   - Android pooche to "Install from unknown sources" allow karo
   - Bas, app install ho jayegi

## Manual rebuild

Repo ke **Actions** tab > "Build Android APK" > "Run workflow" button dabake jab marzi dobara build kar sakte ho.

## Note

- Yeh **debug APK** banata hai — install karne ke liye signing ki zaroorat nahi.
- Release (signed) APK chahiye to keystore set karna padega — bata dena, woh bhi ho jayega.
