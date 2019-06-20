unit RaiseCastedException;

interface

implementation

procedure ProcedureThatCastsAnException;
begin
  raise MyException.Create('Your program is on fire!') as MyOtherException;
end;

end.

