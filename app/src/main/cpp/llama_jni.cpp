#include <jni.h>
#include <string>
#include <android/log.h>
#include "llama.h"
#include "mtmd.h"

#define TAG "llama-jni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static llama_model*   g_model    = nullptr;
static llama_context* g_ctx      = nullptr;
static llama_sampler* g_sampler  = nullptr;
static llama_batch    g_batch    = {};
static mtmd_context*  g_mtmd_ctx = nullptr;

extern "C" {

// ---------------------------------------------------------------------------
// nativeLoad — GGUFモデルをファイルパスからロードする
// 戻り値: 0=成功, 1=失敗
// ---------------------------------------------------------------------------
JNIEXPORT jint JNICALL
Java_com_example_gemma4viewer_engine_LlamaEngine_nativeLoad(
        JNIEnv* env, jobject /* thiz */, jstring modelPath) {
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (!path) {
        LOGE("nativeLoad: GetStringUTFChars failed");
        return 1;
    }
    LOGI("nativeLoad: loading model from %s", path);
    llama_model_params params = llama_model_default_params();
    g_model = llama_model_load_from_file(path, params);
    env->ReleaseStringUTFChars(modelPath, path);
    if (!g_model) {
        LOGE("nativeLoad: llama_model_load_from_file failed");
        return 1;
    }
    LOGI("nativeLoad: model loaded successfully");
    return 0;
}

// ---------------------------------------------------------------------------
// nativePrepare — コンテキスト・サンプラーを初期化する
// 戻り値: 0=成功, 1=失敗
// ---------------------------------------------------------------------------
JNIEXPORT jint JNICALL
Java_com_example_gemma4viewer_engine_LlamaEngine_nativePrepare(
        JNIEnv* /* env */, jobject /* thiz */, jint nCtx, jint nThreads) {
    if (!g_model) {
        LOGE("nativePrepare: g_model is null — call nativeLoad first");
        return 1;
    }
    LOGI("nativePrepare: n_ctx=%d n_threads=%d", (int)nCtx, (int)nThreads);

    llama_context_params ctx_params  = llama_context_default_params();
    ctx_params.n_ctx                 = (uint32_t)nCtx;
    ctx_params.n_threads             = (uint32_t)nThreads;
    ctx_params.n_threads_batch       = (uint32_t)nThreads;

    g_ctx = llama_new_context_with_model(g_model, ctx_params);
    if (!g_ctx) {
        LOGE("nativePrepare: llama_new_context_with_model failed");
        return 1;
    }

    // グリーディサンプラーチェーンを設定
    g_sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(g_sampler, llama_sampler_init_greedy());

    // g_batch はプレースホルダーとして初期化（実際のトークンはTask 5.3で設定）
    g_batch = llama_batch_get_one(nullptr, 0);

    LOGI("nativePrepare: context and sampler initialized");
    return 0;
}

// ---------------------------------------------------------------------------
// nativeSystemInfo — llama.cppシステム情報文字列を返す
// ---------------------------------------------------------------------------
JNIEXPORT jstring JNICALL
Java_com_example_gemma4viewer_engine_LlamaEngine_nativeSystemInfo(
        JNIEnv* env, jobject /* thiz */) {
    const char* info = llama_print_system_info();
    return env->NewStringUTF(info ? info : "");
}

// ---------------------------------------------------------------------------
// nativeUnload — サンプラー・コンテキスト・モデルをこの順に解放する
// ---------------------------------------------------------------------------
JNIEXPORT void JNICALL
Java_com_example_gemma4viewer_engine_LlamaEngine_nativeUnload(
        JNIEnv* /* env */, jobject /* thiz */) {
    LOGI("nativeUnload: releasing resources");
    if (g_mtmd_ctx) {
        mtmd_free(g_mtmd_ctx);
        g_mtmd_ctx = nullptr;
    }
    if (g_sampler) {
        llama_sampler_free(g_sampler);
        g_sampler = nullptr;
    }
    if (g_ctx) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    g_batch = {};
    LOGI("nativeUnload: done");
}

// ---------------------------------------------------------------------------
// 以下はスタブ — Task 5.3 / Task 6 で実装される
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// nativeLoadMmproj — mmprojファイルからmtmdコンテキストを初期化する
// Gemma 4クラッシュ回避: image_min_tokens = 0 を必ず設定する
// 戻り値: 0=成功, 1=失敗
// ---------------------------------------------------------------------------
JNIEXPORT jint JNICALL
Java_com_example_gemma4viewer_engine_LlamaEngine_nativeLoadMmproj(
        JNIEnv* env, jobject /* thiz */, jstring mmprojPath) {
    if (!g_model) {
        LOGE("nativeLoadMmproj: g_model is null — call nativeLoad first");
        return 1;
    }
    const char* path = env->GetStringUTFChars(mmprojPath, nullptr);
    if (!path) {
        LOGE("nativeLoadMmproj: GetStringUTFChars failed");
        return 1;
    }
    LOGI("nativeLoadMmproj: loading mmproj from %s", path);

    mtmd_context_params params = mtmd_context_params_default();
    params.image_min_tokens = 0;  // Gemma 4: 0以外だとクラッシュする

    g_mtmd_ctx = mtmd_init_from_file(path, g_model, params);
    env->ReleaseStringUTFChars(mmprojPath, path);

    if (!g_mtmd_ctx) {
        LOGE("nativeLoadMmproj: mtmd_init_from_file failed");
        return 1;
    }
    LOGI("nativeLoadMmproj: mtmd context initialized successfully");
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_example_gemma4viewer_engine_LlamaEngine_nativeProcessImageTurn(
        JNIEnv* env, jobject /* thiz */,
        jbyteArray rgbBytes, jint width, jint height, jstring prompt) {
    (void)env;
    (void)rgbBytes;
    (void)width;
    (void)height;
    (void)prompt;
    return 0;  // stub — Task 6.3で実装
}

JNIEXPORT jstring JNICALL
Java_com_example_gemma4viewer_engine_LlamaEngine_nativeGenerateNextToken(
        JNIEnv* env, jobject /* thiz */) {
    if (!g_ctx || !g_model || !g_sampler) {
        LOGE("nativeGenerateNextToken: uninitialized state (ctx=%p model=%p sampler=%p)",
             (void*)g_ctx, (void*)g_model, (void*)g_sampler);
        return env->NewStringUTF("");
    }

    // サンプリング: バッチの最後のlogits位置から次トークンを選択
    llama_token token = llama_sampler_sample(g_sampler, g_ctx, -1);

    // EOGトークン（EOS/EOT）検出時は空文字を返す
    const llama_vocab* vocab = llama_model_get_vocab(g_model);
    if (llama_vocab_is_eog(vocab, token)) {
        LOGI("nativeGenerateNextToken: EOG token detected, stopping generation");
        return env->NewStringUTF("");
    }

    // サンプラーの内部状態（repetition penaltyなど）を更新
    llama_sampler_accept(g_sampler, token);

    // 1トークンのバッチを作成してデコード（KVキャッシュに積む）
    g_batch = llama_batch_get_one(&token, 1);
    if (llama_decode(g_ctx, g_batch) != 0) {
        LOGE("nativeGenerateNextToken: llama_decode failed");
        return env->NewStringUTF("");
    }

    // トークンIDをテキスト（UTF-8断片）に変換
    char buf[256] = {};
    int n = llama_token_to_piece(vocab, token, buf, (int)sizeof(buf) - 1, 0, true);
    if (n < 0) {
        LOGE("nativeGenerateNextToken: llama_token_to_piece failed (n=%d)", n);
        return env->NewStringUTF("");
    }
    buf[n] = '\0';
    return env->NewStringUTF(buf);
}

} // extern "C"
