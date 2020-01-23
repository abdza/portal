package portal.FileLink;

import org.springframework.data.jpa.repository.JpaRepository;


public interface FileLinkRepository extends JpaRepository<FileLink, Long> {
	
	FileLink findOneByModuleAndSlug(String module,String slug);

}
