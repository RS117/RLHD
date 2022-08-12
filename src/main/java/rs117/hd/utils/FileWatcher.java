/*
 * Copyright (c) 2022, Hooder <ahooder@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package rs117.hd.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
public class FileWatcher implements AutoCloseable
{
	private static final WatchEvent.Kind<?>[] eventKinds = { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };

	Thread watcherThread;
	WatchService watchService;
	HashMap<WatchKey, Path> watchKeys = new HashMap<>();
	Consumer<Path> changeHandler;

	public FileWatcher(@NonNull Path pathToWatch, @NonNull Consumer<Path> fileChangeHandler) throws IOException
	{
		changeHandler = fileChangeHandler;

		try {
			watchService = FileSystems.getDefault().newWatchService();

			if (pathToWatch.toFile().isFile()) {
				changeHandler = path -> {
					try {
						if (Files.isSameFile(path, pathToWatch))
							fileChangeHandler.accept(path);
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				};
				watchFile(pathToWatch);
			} else {
				watchRecursively(pathToWatch);
			}

			watcherThread = new Thread(() -> {
				try {
					WatchKey key;
					while ((key = watchService.take()) != null) {
						Path dir = watchKeys.get(key);
						if (dir == null) {
							log.error("Unknown WatchKey: " + key);
							continue;
						}
						for (WatchEvent<?> event : key.pollEvents()) {
							if (event.kind() == OVERFLOW)
								continue;

							Path path = dir.resolve((Path) event.context());
							if (path.toString().endsWith("~")) // Ignore temp files
								continue;

							log.trace("WatchEvent of kind {} for path {}", event.kind(), path);

							// Manually register new subfolders if not watching a file tree
							if (event.kind() == ENTRY_CREATE && path.toFile().isDirectory())
								watchRecursively(path);

							changeHandler.accept(path);
						}
						key.reset();
					}
				}
				catch (ClosedWatchServiceException ignored) {}
				catch (InterruptedException ex) {
					throw new RuntimeException("Watcher thread interrupted", ex);
				}
			},  getClass().getSimpleName() + " Thread");

			watcherThread.setDaemon(true);
			watcherThread.start();
		} catch (IOException ex) {
			throw new RuntimeException("Unable to create watch service for shader hot-swap compilation");
		}
	}

	@Override
	public void close() {
		try {
			watchKeys.clear();
			watchService.close();
			watcherThread.join();
		} catch (IOException | InterruptedException ex) {
			throw new RuntimeException("Error while closing " + getClass().getSimpleName(), ex);
		}
	}

	public static Path getResourcePath(Class<?> clazz)
	{
		return Paths.get(
			"src/main/resources",
			clazz.getPackage().getName().replace(".", "/"));
	}

	private void watchFile(Path path) {
		Path dir = path.getParent();
		try {
			watchKeys.put(dir.register(watchService, eventKinds), dir);
			log.trace("Watching {}", dir.toRealPath());
		} catch (IOException ex) {
			throw new RuntimeException("Failed to register file watcher for path: " + path, ex);
		}
	}

	private void watchRecursively(Path path) {
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					WatchKey key = dir.register(watchService, eventKinds);
					log.trace("Watching {}", dir.toRealPath());
					watchKeys.put(key, dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			throw new RuntimeException("Failed to register recursive file watcher for path: " + path, ex);
		}
	}
}
