package org.eclipse.sirius.web.devtools;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.emf.codegen.ecore.generator.Generator;
import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter;
import org.eclipse.emf.common.util.BasicMonitor.Printing;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class GenerateViewModels {

	public static void main(String[] args) {
		System.out.println("Launch Generation");
		// we expect the ${project.basedir} variable as argument
		if (args.length != 1) {
			throw new RuntimeException(
					"invalid argument, we expect the project base dir as first argument (${project.basedir} in Maven");
		}

		String regenProjectLocation = args[0];
		String fileSystemPathToSiriusWeb = "file:" + regenProjectLocation + "/../../";

		Set<String> genmodels = new LinkedHashSet<>();
		genmodels.add("packages/view/backend/sirius-components-view/src/main/resources/model/view.genmodel");

		/*
		 * for each "Eclipse" project register the mapping between it's name and it's
		 * location on the filesystem.
		 */
		EcorePlugin.getPlatformResourceMap().put("sirius-components-view",
				URI.createURI(fileSystemPathToSiriusWeb + "packages/view/backend/sirius-components-view/"));

		final ResourceSet set = new ResourceSetImpl();
		// initialize EPackages
		set.getPackageRegistry().put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);
		set.getPackageRegistry().put(GenModelPackage.eINSTANCE.getNsURI(), GenModelPackage.eINSTANCE);

		// register default XMI resource factory
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());

		// register the EMF generator
		GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor(GenModelPackage.eNS_URI,
				org.eclipse.emf.codegen.ecore.genmodel.generator.GenModelGeneratorAdapterFactory.DESCRIPTOR);

		Monitor monitor = new Printing(System.out);

		for (String genmodelPath : genmodels) {
			String pathOnMyFileSystem = fileSystemPathToSiriusWeb + genmodelPath;
			Generator generator = new Generator();
			generator.getOptions().resourceSet = set;
			generator.getOptions().codeFormatting = true;

			Resource r = set.getResource(URI.createURI(pathOnMyFileSystem), true);
			r.getContents().stream().filter(x -> (x instanceof GenModel)).map(e -> (GenModel) e).forEach(g -> {
				generator.setInput(g);
				g.setCanGenerate(true);
				// generate model code
				generator.generate(g, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE, monitor);
			});
			System.out.println("Done processing " + pathOnMyFileSystem);
		}
	}
}
