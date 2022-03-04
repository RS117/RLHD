/*
 * Copyright (c) 2022, Hooder <https://github.com/ahooder>
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileWatcher implements AutoCloseable
{
	Thread watchThread;
	WatchService watchService;
	HashMap<WatchKey, Path> watchKeys = new HashMap<>();
	HashMap<Integer, BiConsumer<Path, WatchEvent<Path>>> changeHandlers = new HashMap<>();

	private static final WatchEvent.Kind<?>[] eventKinds = { ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY };

	@SuppressWarnings("unchecked")
	public FileWatcher() throws IOException
	{
		watchService = FileSystems.getDefault().newWatchService();
		watchThread = new Thread(() ->
		{
			try
			{
				WatchKey key;
				while ((key = watchService.take()) != null)
				{
					Path watchPath = watchKeys.get(key);
					if (watchPath == null)
					{
						continue;
					}

					if (!watchPath.toFile().exists())
						continue;

					boolean singleFile = watchPath.toFile().isFile();
					Path dir = singleFile ? watchPath.getParent() : watchPath;
					for (WatchEvent<?> event : key.pollEvents())
					{
						Path path = dir.resolve((Path) event.context());
						if (!singleFile || path.compareTo(watchPath) == 0)
						{
							changeHandlers.values().forEach(cb -> cb.accept(path, (WatchEvent<Path>) event));
						}
					}
					key.reset();
				}
			}
			catch (InterruptedException | ClosedWatchServiceException ignored) {}
		});
		watchThread.setDaemon(true);
		watchThread.start();
	}

	/**
	 * Watch all files in the specified path. Not recursive
	 * @param path to a file or directory to watch
	 * @return the same FileWatcher instance
	 * @throws IOException if the path is inaccessible
	 */
	public FileWatcher watchPath(@NonNull Path path) throws IOException
	{
		path = path.toAbsolutePath();
		Path watchPath = path;
		if (path.toFile().isFile())
		{
			watchPath = path.getParent();
			if (watchPath == null)
			{
				throw new IOException("Invalid path: " + path);
			}
		}
		if (!watchPath.toFile().exists())
		{
			throw new FileNotFoundException("Directory does not exist: " + watchPath);
		}
		WatchKey key = watchPath.register(watchService, eventKinds);
		watchKeys.put(key, path);
		return this;
	}

	public FileWatcher watchFile(@NonNull Path path) throws IOException
	{
		path = path.toAbsolutePath();
		if (!path.toFile().exists())
		{
			log.warn("Watched file does not currently exist: {}", path);
		}
		if (path.getParent() == null)
		{
			throw new IOException("Invalid path: " + path);
		}
		WatchKey key = path.getParent().register(watchService, eventKinds);
		watchKeys.put(key, path);
		return this;
	}

	public FileWatcher unwatchPath(Path path)
	{
		path = path.toAbsolutePath();
		for (Map.Entry<WatchKey, Path> entry : watchKeys.entrySet())
		{
			if (entry.getValue().toAbsolutePath().compareTo(path) == 0)
			{
				entry.getKey().cancel();
				watchKeys.remove(entry.getKey());
			}
		}
		return this;
	}

	public FileWatcher addChangeHandler(Consumer<Path> changeHandler)
	{
		return addChangeHandler((path, event) -> changeHandler.accept(path));
	}

	public FileWatcher addChangeHandler(BiConsumer<Path, WatchEvent<Path>> changeHandler)
	{
		changeHandlers.put(changeHandler.hashCode(), changeHandler);
		return this;
	}

	public FileWatcher removeChangeHandler(Consumer<Path> changeHandler)
	{
		changeHandlers.remove(changeHandler.hashCode());
		return this;
	}

	public FileWatcher removeChangeHandler(BiConsumer<Path, WatchEvent<Path>> changeHandler)
	{
		changeHandlers.remove(changeHandler.hashCode());
		return this;
	}

	@Override
	public void close() throws IOException, InterruptedException
	{
		watchKeys.clear();
		watchService.close();
		watchThread.join();
		changeHandlers.clear();
	}
}
