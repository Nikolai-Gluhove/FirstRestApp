package ru.glukhov.springcourse.FirstRestApp.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.glukhov.springcourse.FirstRestApp.dto.PersonDTO;
import ru.glukhov.springcourse.FirstRestApp.models.Person;
import ru.glukhov.springcourse.FirstRestApp.services.PeopleService;
import ru.glukhov.springcourse.FirstRestApp.util.PersonErrorResponse;
import ru.glukhov.springcourse.FirstRestApp.util.PersonNotCreatedException;
import ru.glukhov.springcourse.FirstRestApp.util.PersonNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;
    private final ModelMapper modelMapper;

    @Autowired
    public PeopleController(PeopleService peopleService, ModelMapper modelMapper) {
        this.peopleService = peopleService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public List<PersonDTO> getPeople(){
        return peopleService.findAll().stream().map(this::convertToPersonDTO).collect(Collectors.toList()); //jackson автоматически конвертирует объекты в JSON
    }

    @GetMapping("/{id}")
    public PersonDTO getPerson(@PathVariable("id") int id){
        return convertToPersonDTO(peopleService.findOne(id)); //jackson автоматически конвертирует объекты в JSON
    }

    //В  HTTP ответе тело ответа (response) и статус в заголовке
    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotFoundException e){
        PersonErrorResponse response = new PersonErrorResponse("Person with this id wasn't found!", System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); //NOT_FOUND - 404 статус
    }

    //метод создания нового человека
    @PostMapping
    private ResponseEntity<HttpStatus> create(@RequestBody @Valid PersonDTO personDTO,
                                              BindingResult bindingResult){
        //Если не валидный Json
        if (bindingResult.hasErrors()){
            StringBuilder errorMsg = new StringBuilder();

            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors){
                errorMsg.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append(";");
            }

            throw  new PersonNotCreatedException(errorMsg.toString());
        }
        peopleService.save(convertToPerson(personDTO));

        //http ответ с пустым телом и со статусом 200
        return ResponseEntity.ok(HttpStatus.OK);
    }

    //перевод dto в модель
    private Person convertToPerson(@Valid PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }



    //обработка ошибки PersonNotCreatedException
    @ExceptionHandler
    private  ResponseEntity<PersonErrorResponse> handleException(PersonNotCreatedException e){
        PersonErrorResponse response = new PersonErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //перевод из person в personDTO
    private PersonDTO convertToPersonDTO(Person person){
        return  modelMapper.map(person, PersonDTO.class);
    }
}
































