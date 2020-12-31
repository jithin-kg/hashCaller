#include "secrets.hpp"

#include <jni.h>

#include "sha256.hpp"
#include "sha256.cpp"

/* Copyright (c) 2020-present Klaxit SAS
*
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
*
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
* OTHER DEALINGS IN THE SOFTWARE.
*/

void customDecode(char *str) {
    /* Add your own logic here
    * To improve your key security you can encode it before to integrate it in the app.
    * And then decode it with your own logic in this function.
    */
}

jstring getOriginalKey(
        char *obfuscatedSecret,
        int obfuscatedSecretSize,
        jstring obfuscatingJStr,
        JNIEnv *pEnv) {

    // Get the obfuscating string SHA256 as the obfuscator
    const char *obfuscatingStr = pEnv->GetStringUTFChars(obfuscatingJStr, NULL);
    const char *obfuscator = sha256(obfuscatingStr);

    // Apply a XOR between the obfuscated key and the obfuscating string to get original sting
    char out[obfuscatedSecretSize + 1];
    for (int i = 0; i < obfuscatedSecretSize; i++) {
        out[i] = obfuscatedSecret[i] ^ obfuscator[i % strlen(obfuscator)];
    }

    // Add string terminal delimiter
    out[obfuscatedSecretSize] = 0x0;

    //(Optional) To improve key security
    customDecode(out);

    return pEnv->NewStringUTF(out);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_nibble_hashcaller_Secrets_getbcf2a937004d5b229fdaff17b9fd6d0328d3eb80a709e8234ede7c5501af648b(
        JNIEnv *pEnv,
        jobject pThis,
        jstring packageName) {
    char obfuscatedSecret[] = { 0x4a, 0xb, 0x44, 0x42, 0x73, 0x4, 0x4d, 0x31, 0x5b, 0x2d, 0x4, 0x54, 0x46, 0x46, 0x6, 0x51, 0x43, 0x3 };
    return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_nibble_hashcaller_Secrets_getIBZQHPWG(
        JNIEnv* pEnv,
        jobject pThis,
        jstring packageName) {
     char obfuscatedSecret[] = { 0x51, 0x7, 0x57, 0x2, 0x59, 0x58, 0x7, 0x52, 0x4, 0x52, 0x52, 0x56, 0x6, 0x57, 0x57, 0x2, 0xe, 0x0, 0x6, 0x7, 0x5f, 0x7, 0x8, 0x2, 0x52, 0xc, 0x55, 0x55, 0x3, 0x1, 0x9, 0x51, 0x5, 0x5a, 0x1, 0x51, 0x54, 0x0, 0x0, 0x53, 0x50, 0x55, 0x6, 0xf, 0x4, 0x1, 0x3, 0x2, 0xd, 0x5d, 0x5d, 0x52, 0xe, 0x0, 0x54, 0x6, 0x53, 0x54, 0x59, 0x5, 0x55, 0x3, 0x8, 0x50 };
     return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_nibble_hashcaller_Secrets_getSGBEDOKF(
        JNIEnv* pEnv,
        jobject pThis,
        jstring packageName) {
     char obfuscatedSecret[] = { 0x51, 0x7, 0x57, 0x2, 0x59, 0x58, 0x7, 0x52, 0x4, 0x52, 0x52, 0x56, 0x6, 0x57, 0x57, 0x2, 0xe, 0x0, 0x6, 0x7, 0x5f, 0x7, 0x8, 0x2, 0x52, 0xc, 0x55, 0x55, 0x3, 0x1, 0x9, 0x51, 0x5, 0x5a, 0x1, 0x51, 0x54, 0x0, 0x0, 0x53, 0x50, 0x55, 0x6, 0xf, 0x4, 0x1, 0x3, 0x2, 0xd, 0x5d, 0x5d, 0x52, 0xe, 0x0, 0x54, 0x6, 0x53, 0x54, 0x59, 0x5, 0x55, 0x3, 0x8, 0x50 };
     return getOriginalKey(obfuscatedSecret, sizeof(obfuscatedSecret), packageName, pEnv);
}
