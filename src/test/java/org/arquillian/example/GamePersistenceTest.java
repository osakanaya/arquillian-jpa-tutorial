package org.arquillian.example;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GamePersistenceTest {
	@Deployment
	public static Archive<?> createDeployment() {
		JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jpatest.jar")
			.addPackage(Game.class.getPackage())
			.addAsManifestResource("test-persistence.xml", "persistence.xml")
			.addAsManifestResource("jbossas-ds.xml")
			.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		System.out.println(archive.toString(true));
		
		return archive;
	}
	
	private static final String[] GAME_TITLES = {
		"Super Mario Brothers",
		"Mario Cart",
		"F-Zero"
	};
	
	@PersistenceContext
	EntityManager em;
	
	@Inject
	UserTransaction userTransaction;
	
	@Before
	public void setUp() throws Exception {
		clearData();
		insertData();
		startTransaction();
	}
	
	private void clearData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();
		
		System.out.println("Dumping old records...");
		
		em.createQuery("delete from Game").executeUpdate();
		userTransaction.commit();
	}
	
	private void insertData() throws Exception {
		userTransaction.begin();
		em.joinTransaction();
		
		System.out.println("Inserting records...");
		
		for(String title : GAME_TITLES) {
			Game game = new Game(title);
			em.persist(game);
		}
		
		userTransaction.commit();
		em.clear();
	}
	
	private void startTransaction() throws Exception {
		userTransaction.begin();
		em.joinTransaction();
	}
	
	@Test
	public void shouldFindAllGamesUsingJpqlQuery() throws Exception {
		String fetchingAllGamesInJpql = "select g from Game g order by g.id";
		
		System.out.println("Selecting (using JPQL)...");
		
		List<Game> games = em.createQuery(fetchingAllGamesInJpql, Game.class).getResultList();
		
		System.out.println("Found " + games.size() + " games (using JPQL)");
		
		assertContrainsAllGames(games);
	}
	
	private static void assertContrainsAllGames(Collection<Game> retrievedGames) {
		assertThat(retrievedGames.size(), is(GAME_TITLES.length));
		
		final Set<String> retrievedGameTitles = new HashSet<String>();
		for(Game game : retrievedGames) {
			System.out.println("* " + game);
			retrievedGameTitles.add(game.getTitle());
		}
		
		assertThat(retrievedGameTitles, hasItems(GAME_TITLES));
	}
	
	
	
}
