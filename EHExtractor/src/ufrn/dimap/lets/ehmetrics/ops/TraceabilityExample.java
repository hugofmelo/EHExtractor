package ufrn.dimap.lets.ehmetrics.ops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ufrn.dimap.lets.ehmetrics.ProjectsUtil;

public class TraceabilityExample
{
	// EXEMPLO 1 - IMPLEMENTAÇÃO JAVA NORMAL
	// O método recebe (1) um arquivo ZIP, (2) um caminho para uma pasta e (3) um nome de projeto.
	// O método deve dezipar o arquivo dentro da pasta indicada por (2) e renomear a pasta para (3).
	public static File unZipTo1(File zipFile, String targetFolderPath, String projectRootName) throws IOException
	{
		byte[] buffer = new byte[1024];

		//get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));

		// A primeira entry é a raiz do projeto. Vamos armazenar para retorná-la ao final
		ZipEntry ze = zis.getNextEntry();
		File projectRoot = new File(targetFolderPath + File.separator + ze.getName());
		projectRoot.mkdirs();

		// Efetivamente descompacta o arquivo zip
		ze = zis.getNextEntry();	
		while(ze != null)
		{
			String fileName = ze.getName();
			File newFile = new File(targetFolderPath + File.separator + fileName);

			if ( ze.isDirectory() )
			{
				newFile.mkdirs();
			}
			else
			{
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0)
				{
					fos.write(buffer, 0, len);
				}

				fos.close();
			}

			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();


		// Renomear a raiz para o nome dado pelo argumento
		File projectRootNewName = new File ( ProjectsUtil.PROJECTS_ROOT + File.separator + projectRootName );

		// Verificar se houve erro
		if ( !projectRoot.renameTo(projectRootNewName) )
		{
			throw new IllegalStateException ("Falha ao renomear raiz do projeto dezipado.");
		}

		return projectRootNewName;
	}

	// EXEMPLO 2 - IMPLMENTAÇÃO JAVA PROPOSTA
	// O método recebe (1) um arquivo ZIP, (2) um caminho para uma pasta e (3) um nome de projeto.
	// O método deve dezipar o arquivo dentro da pasta indicada por (2) e renomear a pasta para (3).
	public static File unZipTo2(File zipFile, String targetFolderPath, String projectRootName) throws FileNotFoundException
		//throws IllegalStateException 1
		//rethrows FileNotFoundException 2
		//rethrows IOException 8
	{
		byte[] buffer = new byte[1024];

		//get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));

		// A primeira entry é a raiz do projeto. Vamos armazenar para retorná-la ao final
		ZipEntry ze = zis.getNextEntry();
		File projectRoot = new File(targetFolderPath + File.separator + ze.getName());
		projectRoot.mkdirs();

		// Efetivamente descompacta o arquivo zip
		ze = zis.getNextEntry();	
		while(ze != null)
		{
			String fileName = ze.getName();
			File newFile = new File(targetFolderPath + File.separator + fileName);

			if ( ze.isDirectory() )
			{
				newFile.mkdirs();
			}
			else
			{
				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0)
				{
					fos.write(buffer, 0, len);
				}

				fos.close();
			}

			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();


		// Renomear a raiz para o nome dado pelo argumento
		File projectRootNewName = new File ( ProjectsUtil.PROJECTS_ROOT + File.separator + projectRootName );

		// Verificar se houve erro
		if ( !projectRoot.renameTo(projectRootNewName) )
		{
			throw new IllegalStateException ("Falha ao renomear raiz do projeto dezipado.");
		}

		return projectRootNewName;
	}
}
