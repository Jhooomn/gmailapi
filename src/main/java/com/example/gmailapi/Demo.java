package com.example.gmailapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Demo {

	private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	// private static final Set<String> SCOPES = GmailScopes.all();
	private static final List<String> SCOPES = Arrays.asList(GmailScopes.MAIL_GOOGLE_COM);
	private static final String CREDENTIALS_FILE_PATH = "/fe.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws Exception
	 */

	static final AtomicInteger consultas = new AtomicInteger();
	static final AtomicInteger mensajes = new AtomicInteger();
	static final AtomicInteger error = new AtomicInteger();
	static final AtomicInteger ok = new AtomicInteger();
	static final long INICIO = System.currentTimeMillis();
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
		
		// Load client secrets.
		InputStream in = Demo.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void ejecutar() throws Exception {
		// Build a new authorized API client service.
		long init = System.currentTimeMillis();
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the labels in the user's account.
		String user = "me";
		listLabels(service, user);
		listMessage(service, user, null);
		log.info("duracion total:" + (System.currentTimeMillis() - init) + " ms");
		log.info("Consultas {}, Mensajes {} ", consultas.get(), mensajes.get());
	}

	private static void listMessage(Gmail service, String user, String pageToken) throws IOException {
		consultas.incrementAndGet();
		ListMessagesResponse listResponse = service.users().messages().list(user).setPageToken(pageToken).execute();
		List<Message> messages = listResponse.getMessages();
		if (messages.isEmpty()) {
			log.info("No messages found.");
		} else {
			messages.parallelStream().forEach(message -> {
				getMessage(service, user, message.getId());
			});

		}
		
		log.info("Mensajes consultados: {}, tiempo estimado:{} segundos" , mensajes.get(), (System.currentTimeMillis() - INICIO)/1000);
		listMessage(service, user, listResponse.getNextPageToken());
	}

	private static Message getMessage(Gmail service, String user, String id) {
		mensajes.getAndIncrement();
		try {
			return service.users().messages().get(user, id).execute();
		} catch (IOException e) {
			return null;
		}

	}

	private static void listLabels(Gmail service, String user) throws IOException {
		ListLabelsResponse listResponse = service.users().labels().list(user).execute();
		List<Label> labels = listResponse.getLabels();
		if (labels.isEmpty()) {
			log.info("No labels found.");
		} else {
			log.info("Labels:");
			for (Label label : labels) {
				System.out.printf("- %s\n", label.getName());
			}
		}
	}
}