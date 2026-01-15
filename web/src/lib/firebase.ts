import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
    apiKey: "AIzaSyD6GjC_BRWlJqNyjjqyaSUl5np2tvMdWfU",
    authDomain: "salary-tracker-c41cc.firebaseapp.com",
    projectId: "salary-tracker-c41cc",
    storageBucket: "salary-tracker-c41cc.firebasestorage.app",
    messagingSenderId: "223346959208",
    appId: "1:223346959208:web:6f0f2bcd8d187e6b8becdd" // You may need to update this with the actual Web App ID from Firebase Console
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
