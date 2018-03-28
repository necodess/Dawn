package me.saket.dank.cache;

import com.nytimes.android.external.fs3.PathResolver;
import com.nytimes.android.external.fs3.Util;

import java.util.Locale;
import javax.annotation.Nonnull;

import me.saket.dank.utils.Strings;
import timber.log.Timber;

public abstract class DiskLruCachePathResolver<KEY> implements PathResolver<KEY> {

  private static final Util UTIL = new Util();

  @Nonnull
  @Override
  public String resolve(@Nonnull KEY key) {
    String path = resolveIn64Letters(key);

    if (path.length() > 64) {
      Timber.w("Path will be truncated: %s", key);
    }

    String resolvedPath = Strings.substringWithBounds2(path, 64);
    return makeCompatibleWithDiskLruCache(resolvedPath);
  }

  private String makeCompatibleWithDiskLruCache(String resolvedPath) {
    // Store library seems to clean paths before using them. This class does the same thing.
    String simplifiedPath = UTIL.simplifyPath(resolvedPath);
    if (simplifiedPath.startsWith("/")) {
      simplifiedPath = simplifiedPath.substring(1);
    }

    // DiskLruCache only allows: [a-z0-9_-]{1,64}
    // Reddit only allows '_' and '-' in user-names.
    return simplifiedPath
        .toLowerCase(Locale.ENGLISH)
        .replaceAll("/", "--")
        .replaceAll("\\.", "_");
  }

  /**
   * Generate a key which will be unique even after it's truncated to 64 characters.
   */
  protected abstract String resolveIn64Letters(KEY key);
}
