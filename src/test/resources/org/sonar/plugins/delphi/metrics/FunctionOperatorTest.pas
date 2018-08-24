unit FunctionOperatorTest;

interface

type
  IGenericA<T> = interface
  end;

  GenericA<T> = record
  public
    class operator Implicit(const value: GenericA<T>): IGenericA<T>;
  end;

implementation

{ GenericA<T> }

class operator GenericA<T>.Implicit(const value: GenericA<T>): IGenericA<T>;
begin
  Result := value;
end;

end.
