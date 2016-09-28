package org.siani.legio;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import tara.StashBuilder;

import java.io.File;

@Ignore
public class AcceptedStashBuilder {

	@Test
	public void should_create_stash() throws Exception {
		final File tara = new File("/Users/oroncal/workspace/tara/ide/legio/legio-core/test/legio/Example.tara");
		StashBuilder builder = new StashBuilder(tara, "Legio", "legio");
		Assert.assertNotNull(builder.build());
	}
}
