# Consumer ProGuard rules for TimelineView.
# Okio's optional nullability annotation is absent from its runtime dependencies.
# It is metadata only, not a class called by Timeline/Lottie. Keep R8 diagnostics
# enabled for all other missing classes; do not keep the whole library.
-dontwarn javax.annotation.Nullable
