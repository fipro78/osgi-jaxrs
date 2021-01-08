package org.fipro.modifier.jaxrs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fipro.modifier.api.StringModifier;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

@Component(service=InverterRestService.class)
@JaxrsResource
@Produces(MediaType.APPLICATION_JSON)
@JSONRequired
@HttpWhiteboardResource(pattern = "/files/*", prefix = "static")
public class InverterRestService {
	
	@Reference
	private volatile List<StringModifier> modifier;
	
	@GET
	@Path("modify/{input}")
	public List<String> modify(@PathParam("input") String input) {
		return modifier.stream()
				.map(mod -> mod.modify(input))
				.collect(Collectors.toList());
	}
	
	@POST
	@Path("modify/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public Response upload(@Context HttpServletRequest request) 
	        throws IOException, ServletException {

	    // get the part with name "file" received by within
	    // a multipart/form-data POST request
	    Part part = request.getPart("file");
	    if (part != null 
	            && part.getSubmittedFileName() != null 
	            && part.getSubmittedFileName().length() > 0) {

	        StringBuilder inputBuilder = new StringBuilder();
	        try (InputStream is = part.getInputStream();
	                BufferedReader br = 
	                    new BufferedReader(new InputStreamReader(is))) {

	            String line;
	            while ((line = br.readLine()) != null) {
	                inputBuilder.append(line).append("\n");
	            }
	        }
			
	        // modify file content
	        String input = inputBuilder.toString();
	        List<String> modified = modifier.stream()
	            .map(mod -> mod.modify(input))
	            .collect(Collectors.toList());

	            return Response.ok(String.join("\n\n", modified)).build();
	    }

	    return Response.status(Status.PRECONDITION_FAILED).build();
	}
}